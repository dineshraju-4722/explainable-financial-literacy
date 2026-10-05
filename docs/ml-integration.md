# Spring Boot ↔ FastAPI ML Integration Architecture

## 1. Architecture Overview

```
┌─────────┐        ┌─────────────────────┐        ┌──────────────────────┐        ┌─────────────────────────┐
│  React  │ ─────> │ Spring Boot Backend │ ─────> │  FastAPI ML Service  │ ─────> │  TOP_75 LightGBM Model  │
│Frontend │        │   (Orchestrator)    │        │  (Inference Engine)  │        │ (Frozen Phase 6C-R Model)│
└─────────┘        └─────────────────────┘        └──────────────────────┘        └─────────────────────────┘
```

- **React Frontend**: Interacts exclusively with the Spring Boot backend REST APIs. The frontend does **NOT** directly communicate with the FastAPI service.
- **Spring Boot Backend**: Serves as the primary application and security orchestration layer. It validates assessment response completeness, enforces business rules, calls the ML prediction service, and persists archetype predictions.
- **FastAPI ML Service**: Python microservice exposing fast inference and instance-level TreeSHAP explanation endpoints over HTTP using the locked Phase 6C-R `TOP_75` LightGBM classifier.
- **TOP_75 LightGBM Model**: Frozen multiclass LightGBM classifier operating on 75 original NFCS predictor variables ($503$ one-hot encoded categories).

---

## 2. Configuration & Timeouts

The FastAPI service URL and HTTP connection timeouts are fully externalized in [`application.yml`](file:///b:/Rough/CS-Project/backend/src/main/resources/application.yml):

```yaml
app:
  ml-service:
    url: ${ML_SERVICE_URL:http://localhost:8000}
    connect-timeout-ms: ${ML_SERVICE_CONNECT_TIMEOUT:3000}
    read-timeout-ms: ${ML_SERVICE_READ_TIMEOUT:5000}
```

- **Local Development**: `http://localhost:8000`
- **Docker Compose Networking**: `http://ml-service:8000` (via `fl_network` bridge)
- **Timeouts**: 
  - Connection Timeout: `3000 ms` (3 seconds)
  - Read Timeout: `5000 ms` (5 seconds)

---

## 3. API Contracts

### A. Archetype Prediction Endpoint (`POST /predict`)
Spring Boot serializes features into [`MlPredictionRequest`](file:///b:/Rough/CS-Project/backend/src/main/java/com/financialliteracy/dto/ml/MlPredictionRequest.java) and receives [`MlPredictionResponse`](file:///b:/Rough/CS-Project/backend/src/main/java/com/financialliteracy/dto/ml/MlPredictionResponse.java):

```json
{
  "archetype": "OVERCONFIDENT",
  "probabilities": {
    "COMPETENT": 0.0245,
    "OVERCONFIDENT": 0.8444,
    "UNDERCONFIDENT": 0.0381,
    "NAIVE": 0.0929
  },
  "model_version": "1.0.0",
  "timestamp": "2026-10-04T20:50:11.123456+00:00",
  "metadata": {
    "model_name": "LightGBM_TOP75_ClassWeighted",
    "macro_f1": 0.5205,
    "accuracy": 0.5619
  }
}
```

### B. Instance-Level TreeSHAP Explanation Endpoint (`POST /explain`)
Spring Boot serializes features into [`MlExplanationRequest`](file:///b:/Rough/CS-Project/backend/src/main/java/com/financialliteracy/dto/ml/MlExplanationRequest.java) and receives [`MlExplanationResponse`](file:///b:/Rough/CS-Project/backend/src/main/java/com/financialliteracy/dto/ml/MlExplanationResponse.java):

#### Request Body (`POST /api/ml/explain` or `POST /explain`):
```json
{
  "features": {
    "M1_1": "2",
    "J20": "4",
    "J1": "7"
    ... (All 75 TOP_75 NFCS predictor variables)
  },
  "target_class": "UNDERCONFIDENT",
  "top_k": 5
}
```

#### Response Body:
```json
{
  "archetype": "UNDERCONFIDENT",
  "probabilities": {
    "COMPETENT": 0.0512,
    "OVERCONFIDENT": 0.0821,
    "UNDERCONFIDENT": 0.7814,
    "NAIVE": 0.0853
  },
  "model_version": "1.0.0",
  "timestamp": "2026-10-04T21:10:00.123456+00:00",
  "explanation": {
    "target_class": "UNDERCONFIDENT",
    "top_k": 5,
    "top_positive_contributors": [
      {
        "feature": "J1",
        "description": "Difficulty Covering Monthly Expenses",
        "shap_value": 0.1245
      }
    ],
    "top_negative_contributors": [
      {
        "feature": "M1_1",
        "description": "Overall Financial Knowledge (Self-Assessment Scale 1-7)",
        "shap_value": -0.0872
      }
    ]
  }
}
```

---

## 4. TreeSHAP Aggregation & Non-Causal Interpretation Rules

### Original-Variable Aggregation Methodology
1. **Model Representation**: LightGBM operates internally on 75 original variables $\to$ 503 one-hot encoded binary features.
2. **TreeSHAP Calculation**: `shap.TreeExplainer` calculates signed SHAP contributions across all 503 encoded categories for the specified target class.
3. **Variable Aggregation**: For each original NFCS predictor $v$, signed SHAP values are aggregated across all one-hot encoded categories belonging to $v$:
$$\text{SHAP}(v) = \sum_{c \in \text{encoded}(v)} \text{SHAP}(c)$$
4. **Top Contributors**: Features are split into `top_positive_contributors` (shifting prediction **toward** target class) and `top_negative_contributors` (shifting prediction **away** from target class), capped at `top_k` (default 5, max 10).

### Non-Causal Language Guidelines
- **Correct Language**: "Feature $X$ contributed toward the model's prediction of archetype $Y$."
- **Forbidden Causal Claims**: Do NOT claim that $X$ "causes", "determines", or "proves" psychological mechanisms or population-level financial behaviors. SHAP explains internal tree splits of the model, not causal real-world mechanisms.

---

## 5. TOP_75 Feature Completeness & Validation

Before triggering a prediction or explanation, [`MlPredictionServiceImpl`](file:///b:/Rough/CS-Project/backend/src/main/java/com/financialliteracy/service/impl/MlPredictionServiceImpl.java) programmatically validates that all **75 required NFCS variables** are present in the request features map.

- **Validation Rule**: If any required TOP_75 predictor is absent, the backend aborts the ML request and throws [`IncompleteFeatureVectorException`](file:///b:/Rough/CS-Project/backend/src/main/java/com/financialliteracy/exception/IncompleteFeatureVectorException.java), returning a `400 BAD_REQUEST` response containing the exact missing variable codes.
- **No Silently Fabricated Values**: Missing variables are never silently defaulted to arbitrary constants (`0`, `"0"`, `false`, or `"unknown"`).

---

## 6. Persistence Schema (`V3__add_ml_archetype_fields.sql`)

Flyway migration [`V3__add_ml_archetype_fields.sql`](file:///b:/Rough/CS-Project/backend/src/main/resources/db/migration/V3__add_ml_archetype_fields.sql) updates the `assessment_results` table:

```sql
ALTER TABLE assessment_results ADD COLUMN ml_archetype VARCHAR(50);
ALTER TABLE assessment_results ADD COLUMN ml_model_version VARCHAR(50);
ALTER TABLE assessment_results ADD COLUMN ml_predicted_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE assessment_results ADD COLUMN ml_probabilities_json TEXT;
```

---

## 7. Error Handling & Resilience

| Failure Mode | Exception Class | HTTP Status | Response Handling |
| :--- | :--- | :--- | :--- |
| **Incomplete TOP_75 Vector** | `IncompleteFeatureVectorException` | `400 BAD_REQUEST` | Returns missing feature names list without calling ML service |
| **Invalid top_k / Class** | `BadRequestException` / `HTTPException` | `400 BAD_REQUEST` | Rejects out-of-bounds parameters (`top_k` outside 1..10) |
| **ML Service Unreachable / Timeout** | `MlServiceUnavailableException` | `503 SERVICE_UNAVAILABLE` | Returns clean operational error without crashing core backend |
| **FastAPI HTTP 4xx / 5xx Error** | `MlServiceException` | `500 INTERNAL_SERVER_ERROR` | Logs error detail internally, returns sanitized error response |

---

## 8. Testing & Verification

1. **FastAPI Test Suite** ([`ml-service/tests/run_tests.py`](file:///b:/Rough/CS-Project/ml-service/tests/run_tests.py)): Verifies `/health`, `/predict`, `/explain`, prediction/explanation consistency within $10^{-4}$ probability tolerance, `top_k` limits, and input validation.
2. **Spring Boot Unit Tests** ([`MlIntegrationTest`](file:///b:/Rough/CS-Project/backend/src/test/java/com/financialliteracy/ml/MlIntegrationTest.java)): Verifies DTO deserialization, feature validation, and exception handling.
3. **Real Unmocked Integration Test** ([`RealFastApiIntegrationTest`](file:///b:/Rough/CS-Project/backend/src/test/java/com/financialliteracy/ml/RealFastApiIntegrationTest.java)): Executes real unmocked HTTP requests from Spring Boot to live FastAPI process on `http://localhost:8000`, confirming real prediction accuracy, exact consistency, and complete TreeSHAP driver breakdown.
