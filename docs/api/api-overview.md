# API Overview Specification

## Backend Endpoints (`/api/...`)

### System & Health
- `GET /api/health`: System status, backend version, DB state, and Python ML service connectivity status.

### Authentication & Authorization (Phase 3)
- `POST /api/auth/register`: Create user account.
- `POST /api/auth/login`: Authenticate and receive JWT token.
- `GET /api/auth/me`: Current profile context.

### Question Engine (Phase 4)
- `GET /api/questions`: Retrieve active assessment questions.
- `GET /api/categories`: Retrieve list of financial literacy domains.

### Assessment & Diagnosis (Phase 4 & 7)
- `POST /api/assessments`: Submit assessment answers.
- `GET /api/assessments/{id}/result`: View assessment summary, ML prediction, and weak areas.

### ML Service Bridge (Phase 7 & 11)
- `POST /api/financial-literacy/predict`: Proxy request to Python ML service.
- `POST /api/financial-literacy/explain`: Proxy request to Python SHAP explanation endpoint.

---

## Python ML Service Endpoints (`/health`, `/predict`, `/explain`)

- `GET /health`: ML service operational status, loaded model artifact meta.
- `POST /predict`: Input feature vector -> Returns predicted class and probabilities.
- `POST /explain`: Input feature vector -> Returns feature attribution values (SHAP).
- `POST /train`: Trigger experimental model training & evaluation export.
