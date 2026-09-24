# Machine Learning Dataset & Research Design Specification (Phase 5A)

## Executive Overview

This document specifies the research design, machine learning problem formulation, target variable analysis, feature taxonomy, data leakage prevention rules, and evaluation strategy for Phase 5A of the **Explainable Machine Learning-Based Financial Literacy Assessment and Personalized Financial Education System**.

Phase 5A serves as the formal **Research & Architecture Design Phase** preceding any ML model training (Phase 5B) or SHAP explainability implementation (Phase 6). This specification establishes the scientific foundation to ensure that the machine learning system provides meaningful analytical value beyond the Phase 3 deterministic scoring baseline.

---

## 1. Research Objective & Scope

### 1.1 Context & Core Objective
Phases 1–4 established the core platform infrastructure, PostgreSQL domain models, Spring Boot assessment scoring engine, and Phase 4 feature vector generation (`AssessmentFeatureVector`). Phase 3 calculates a baseline deterministic percentage score and assigns a proficiency level (`BEGINNER`, `INTERMEDIATE`, `ADVANCED`).

The objective of Phase 5 is to transition from simple score-based rule classification to an explainable, data-driven machine learning system.

> **Primary Research Objective**:
> *Can machine learning be used to assess or classify financial-literacy characteristics using structured assessment and participant-related financial-literacy features, while providing interpretable explanations and personalized educational guidance?*

### 1.2 Phase 5A Boundary
Phase 5A is strictly limited to research design, feature mapping, data schema specification, anti-leakage rules, and API contract specification. Model training, package installations (e.g., LightGBM/XGBoost), synthetic data generation, and prediction API implementations are deferred to Phase 5B.

---

## 2. Research Questions

To evaluate the core objective scientifically, the ML research methodology addresses the following structured questions:

### 2.1 Primary Research Question (PRQ)
* **PRQ**: How accurately and reliably can a machine-learning classification model predict a participant's overall financial capability archetype or multi-dimensional financial risk profile using domain-specific category performance metrics, difficulty breakdown, and participant background characteristics?

### 2.2 Secondary Research Questions (SRQs)
* **SRQ 1 (Feature Importance & Attribution)**: Which specific financial literacy categories (e.g., `SAVING`, `BUDGETING`, `INTEREST`, `INVESTING`, `DEBT_MANAGEMENT`) contribute most significantly to predicting financial capability mismatches or vulnerability?
* **SRQ 2 (Model Performance Lift)**: How do gradient-boosted decision tree models (e.g., LightGBM) compare against baseline linear models (Logistic Regression) and deterministic score thresholds in identifying complex literacy patterns?
* **SRQ 3 (Explainability Integration)**: Can TreeSHAP generate consistent, mathematically sound local feature attributions that explain why a participant was assigned a specific classification?
* **SRQ 4 (Personalization Translation)**: How can negative SHAP feature attributions be algorithmically mapped to targeted educational modules to guide closed-loop financial learning (Phase 7)?

---

## 3. Machine Learning Problem Definition

The problem is formulated as a **Supervised Multiclass Classification** task (or Supervised Binary Classification, depending on the verified target dataset).

### 3.1 Mathematical Formulation
Given an assessment attempt dataset $\mathcal{D} = \{(\mathbf{x}_i, y_i)\}_{i=1}^N$:
* $\mathbf{x}_i \in \mathcal{X} \subset \mathbb{R}^d$ represents the $d$-dimensional input feature vector constructed from domain metrics, difficulty metrics, and participant context.
* $y_i \in \mathcal{Y} = \{C_1, C_2, \dots, C_K\}$ represents the discrete target label corresponding to $K$ defined financial literacy or capability categories.
* The learning algorithm seeks a parameterization $\theta$ of prediction model $f_\theta: \mathcal{X} \to \Delta^K$ minimizing empirical cross-entropy loss over the training distribution while maximizing generalization performance on held-out test sets.

---

## 4. Target Variable Candidates & Target Leakage Analysis

A critical challenge in applying machine learning to assessment systems is preventing **Target Leakage**. Target leakage occurs when input features contain information derived directly from or identical to the target label, causing artificial, non-generalizable performance.

### 4.1 Evaluation of Candidate Target Variables

```
+-----------------------------------------------------------------------------+
|                         CANDIDATE TARGET EVALUATION                         |
+-----------------------------------------------------------------------------+
| Target Candidate A: Baseline Assessment Proficiency                         |
|   Inputs: overallPercentage, correctAnswers                                 |
|   Target: BEGINNER / INTERMEDIATE / ADVANCED                                |
|   Evaluation: DIRECT TARGET LEAKAGE (Trivial identity function)             |
|   Status: REJECTED AS STANDALONE ML TARGET                                  |
+-----------------------------------------------------------------------------+
| Target Candidate B: Subjective-Objective Capability Mismatch (Zhu 2025)     |
|   Inputs: Category domain scores, background behaviors                      |
|   Target: Financially Competent / Overconfident / Underconfident / Naïve     |
|   Evaluation: SCIENTIFICALLY SOUND (Prevents single-score identity leakage)  |
|   Status: RECOMMENDED PRIMARY CONCEPTUAL TARGET                             |
+-----------------------------------------------------------------------------+
| Target Candidate C: Independent External Financial Capability Benchmark      |
|   Inputs: Assessment domain vectors, participant characteristics            |
|   Target: Validated external financial wellness tier / emergency fund status|
|   Evaluation: SCIENTIFICALLY VALID (Independent external benchmark)          |
|   Status: RECOMMENDED SECONDARY CONCEPTUAL TARGET                           |
+-----------------------------------------------------------------------------+
```

#### Candidate Target A: Baseline Proficiency (`BEGINNER`, `INTERMEDIATE`, `ADVANCED`)
* **Source**: Phase 3 deterministic rule engine ($P_{overall} < 40\% \implies \text{BEGINNER}$, $40-69.9\% \implies \text{INTERMEDIATE}$, $\ge 70\% \implies \text{ADVANCED}$).
* **Leakage Analysis**: If the input feature vector includes `overallPercentage`, `correctAnswers`, or category percentage sums, predicting `proficiencyLevel` is a deterministic mathematical identity. An ML model would simply memorize threshold cutoffs.
* **Decision**: **REJECTED** as a standalone target when overall score features are present.

#### Candidate Target B: Subjective vs. Objective Capability Mismatch (Zhu 2025 Extension)
* **Source**: Extended research methodology adapted from Alex Yue Feng Zhu (2025), *Technology in Society*.
* **Definition**: Classifies participants into 4 distinct capability archetypes by combining objective domain assessment scores with subjective confidence / financial behavior:
  1. `FINANCIALLY_COMPETENT`: High objective knowledge ($\ge 70\%$), high self-confidence.
  2. `OVERCONFIDENT`: Low objective knowledge ($< 70\%$), high self-confidence (*Highest financial risk archetype*).
  3. `UNDERCONFIDENT`: High objective knowledge ($\ge 70\%$), low self-confidence.
  4. `NAIVE_INCOMPETENT`: Low objective knowledge ($< 70\%$), low self-confidence.
* **Leakage Analysis**: Eliminates single-variable target leakage by evaluating the interaction between objective category performance vectors and independent subjective confidence markers.
* **Decision**: **RECOMMENDED PRIMARY CONCEPTUAL TARGET**.

#### Candidate Target C: External Financial Vulnerability Benchmark
* **Source**: Validated external research survey microdata (e.g., FINRA National Financial Capability Study - NFCS) measuring real-world outcomes such as emergency fund capability or high-cost debt distress.
* **Leakage Analysis**: Completely independent of the application's internal assessment score calculation.
* **Decision**: **RECOMMENDED SECONDARY CONCEPTUAL TARGET**.

---

## 5. Dataset Source Strategy & Verification Rule

### 5.1 Dataset Options
1. **Option 1 — Real Collected Participant Data**: Participant assessment records accumulated via the Spring Boot platform database (`assessments`, `assessment_responses`, `users`).
2. **Option 2 — Public Validated Research Dataset**: Microdata from recognized research surveys (e.g., FINRA NFCS 2021/2024 microdata, OECD/INFE International Survey of Adult Financial Literacy).
3. **Option 3 — Synthetic / Generated Data**: **STRICTLY PROHIBITED** for production model training and research validation. Synthetic data must not be presented as empirical research data.

### 5.2 Mandatory Dataset Decision Rule
```
===============================================================================
                     RESEARCH DATASET VERIFICATION RULE
===============================================================================
Dataset Selection Status: PENDING VERIFICATION

Rules:
1. DO NOT automatically inject random Kaggle datasets or synthetic data.
2. DO NOT train models until a dataset is formally reviewed.
3. Candidate datasets must be brought to the research lead to verify:
   - Alignment with the defined research questions
   - Presence of required category and demographic features
   - Absence of target leakage
   - License compatibility and empirical integrity
===============================================================================
```

---

## 6. Dataset Requirements & Feature Taxonomy

### 6.1 Sample Size Requirements
The required dataset size depends on feature dimensionality ($d \approx 15-25$), class balance, and model complexity:
* **Minimum Observations ($N_{min}$)**: 500 validated assessment records.
* **Recommended Observations ($N_{rec}$)**: 2,000+ validated assessment records to ensure 5-fold cross-validation stability and sufficient statistical power ($\ge 0.80$, $\alpha = 0.05$).

### 6.2 Existing Phase 4 Feature Vector Mapping
The Phase 4 Java DTO `AssessmentFeatureVector` provides the baseline feature snapshot:

```
                               AssessmentFeatureVector
                                         │
        ┌────────────────────────────────┼────────────────────────────────┐
        ▼                                ▼                                ▼
  Overall Metrics               Category Metrics                 Difficulty Metrics
  ───────────────               ────────────────                 ──────────────────
  • overallPercentage           • BUDGETING (Count, %*)          • BEGINNER (Count, %*)
  • totalQuestions              • SAVING (Count, %*)             • INTERMEDIATE (Count, %*)
  • answeredQuestions           • CREDIT (Count, %*)             • ADVANCED (Count, %*)
  • correctAnswers              • INVESTING (Count, %*)
  • incorrectAnswers            • INSURANCE (Count, %*)          * Note: % is null if
  • proficiencyLevel            • TAXATION (Count, %*)            category/difficulty
  • assessmentType              • DEBT_MANAGEMENT (Count, %*)     was not attempted.
  • completionStatus            • FINANCIAL_PLANNING (Count, %*)
                                • INTEREST (Count, %*)
                                • INFLATION (Count, %*)
                                • RISK (Count, %*)
```

### 6.3 Comprehensive Data Leakage Table

| Feature Name | Data Type | Target A (Proficiency) Status | Target B (Capability Mismatch) Status | Target C (External Outcome) Status | Leakage Prevention Action |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `overallPercentage` | `Double` | **DIRECT LEAKAGE** | **POTENTIAL LEAKAGE** | **SAFE** | Exclude when target is score-derived; decompose into domain categories |
| `proficiencyLevel` | `String` | **DIRECT LEAKAGE** | **DIRECT LEAKAGE** | **POTENTIAL LEAKAGE** | Exclude entirely from input vector |
| `correctAnswers` | `Integer` | **DIRECT LEAKAGE** | **POTENTIAL LEAKAGE** | **SAFE** | Exclude count sums; rely on category percentages |
| `incorrectAnswers` | `Integer` | **DIRECT LEAKAGE** | **POTENTIAL LEAKAGE** | **SAFE** | Exclude count sums |
| `categoryFeatures[CAT].percentage` | `Double` | **POTENTIAL LEAKAGE** | **SAFE** | **SAFE** | Primary ML input feature set (11 domain features) |
| `difficultyFeatures[DIFF].percentage` | `Double` | **POTENTIAL LEAKAGE** | **SAFE** | **SAFE** | Secondary ML input feature set (3 difficulty features) |
| `assessmentType` | `Enum` | **SAFE** | **SAFE** | **SAFE** | Context variable (`INITIAL` vs `REASSESSMENT`) |
| `completionStatus` | `Enum` | **SAFE** | **SAFE** | **SAFE** | Validation filter (`COMPLETED` required) |
| `age` / `educationLevel` / `occupation` | `Mixed` | **SAFE** | **SAFE** | **SAFE** | Participant background context |
| `subjectiveConfidenceScore` | `Double` | **SAFE** | **TARGET COMPONENT** | **SAFE** | Used in target definition for Target B |

---

## 7. Dataset Schema Specification

The dataset schema for Phase 5B ML ingestion is specified below:

| Column Name | Physical Type | Logical Type | Domain / Range | Missing Value Policy | Leakage Status | ML Model Usage |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `sample_id` | `UUID / String` | Identifier | Unique GUID | Primary key (No nulls) | N/A | Excluded (ID only) |
| `user_id` | `UUID / String` | Group Identifier | Unique GUID | No nulls | N/A | Used for GroupKFold splitting |
| `assessment_type` | `String` | Categorical | `INITIAL`, `REASSESSMENT` | No nulls | SAFE | Encoded feature |
| `cat_budgeting_pct` | `Float64` | Continuous | `[0.0, 100.0]` | `NaN` if unattempted | SAFE | Input Feature |
| `cat_saving_pct` | `Float64` | Continuous | `[0.0, 100.0]` | `NaN` if unattempted | SAFE | Input Feature |
| `cat_credit_pct` | `Float64` | Continuous | `[0.0, 100.0]` | `NaN` if unattempted | SAFE | Input Feature |
| `cat_investing_pct` | `Float64` | Continuous | `[0.0, 100.0]` | `NaN` if unattempted | SAFE | Input Feature |
| `cat_insurance_pct` | `Float64` | Continuous | `[0.0, 100.0]` | `NaN` if unattempted | SAFE | Input Feature |
| `cat_taxation_pct` | `Float64` | Continuous | `[0.0, 100.0]` | `NaN` if unattempted | SAFE | Input Feature |
| `cat_debt_management_pct` | `Float64` | Continuous | `[0.0, 100.0]` | `NaN` if unattempted | SAFE | Input Feature |
| `cat_financial_planning_pct` | `Float64` | Continuous | `[0.0, 100.0]` | `NaN` if unattempted | SAFE | Input Feature |
| `cat_interest_pct` | `Float64` | Continuous | `[0.0, 100.0]` | `NaN` if unattempted | SAFE | Input Feature |
| `cat_inflation_pct` | `Float64` | Continuous | `[0.0, 100.0]` | `NaN` if unattempted | SAFE | Input Feature |
| `cat_risk_pct` | `Float64` | Continuous | `[0.0, 100.0]` | `NaN` if unattempted | SAFE | Input Feature |
| `diff_beginner_pct` | `Float64` | Continuous | `[0.0, 100.0]` | `NaN` if unattempted | SAFE | Input Feature |
| `diff_intermediate_pct` | `Float64` | Continuous | `[0.0, 100.0]` | `NaN` if unattempted | SAFE | Input Feature |
| `diff_advanced_pct` | `Float64` | Continuous | `[0.0, 100.0]` | `NaN` if unattempted | SAFE | Input Feature |
| `age` | `Int64` | Discrete | `[18, 100]` | Impute median | SAFE | Input Feature |
| `education_level` | `String` | Ordinal | `High School`, `Undergraduate`, `Postgraduate` | Mode imputation | SAFE | Encoded Feature |
| `occupation` | `String` | Nominal | Standard codes | Category `OTHER` | SAFE | Encoded Feature |
| `target_capability_archetype` | `String` | Class Label | `COMPETENT`, `OVERCONFIDENT`, `UNDERCONFIDENT`, `NAIVE` | No nulls allowed | TARGET | Target Column ($y$) |

---

## 8. Preprocessing & Data Pipeline Plan

### 8.1 Missing Value Policy (`null` vs `0.0%`)
As established in Phase 4 specification:
* **`0.0%`**: Questions in this category were attempted, but 0 were answered correctly.
* **`null` (`NaN`)**: No questions were attempted in this category for the assessment.
* **Imputation Rule**: Tree-based models (LightGBM/XGBoost) natively branch on missing values without imputation. Linear baseline models (Logistic Regression) require explicit indicator variables (`cat_X_is_missing`) paired with zero-filling to avoid distorting unassessed categories as failed topics.

### 8.2 Categorical & Ordinal Encoding
* Nominal attributes (`assessmentType`, `occupation`): One-Hot Encoding (`OneHotEncoder(sparse_output=False, handle_unknown='ignore')`).
* Ordinal attributes (`educationLevel`): Ordinal Encoding (`High School` $= 0$, `Undergraduate` $= 1$, `Postgraduate` $= 2$).

### 8.3 Feature Scaling
* **Tree Models (LightGBM/XGBoost)**: Invariant to monotonic feature scaling; raw continuous percentages retained.
* **Linear Baselines (Logistic Regression)**: Standardized using `StandardScaler` fitted exclusively on training folds.

---

## 9. Train / Validation / Test & Data Splitting Strategy

### 9.1 Data Partitioning
To ensure rigorous evaluation and prevent data leakage:
* **Training Set**: 70% of verified data (used for model fitting and hyperparameter tuning).
* **Validation Set**: 15% of verified data (used for early stopping during gradient boosting).
* **Held-Out Test Set**: 15% of verified data (strictly reserved for final evaluation).

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          VERIFIED DATASET DISPOSITION                       │
├─────────────────────────────────────────────┬─────────────────┬─────────────┤
│            Training Set (70%)               │ Validation (15%)│  Test (15%) │
│   ┌─────────────────────────────────────┐   │ (Early Stopping)│  (Held-Out) │
│   │ 5-Fold Stratified Group Cross-Val   │   │                 │             │
│   └─────────────────────────────────────┘   │                 │             │
└─────────────────────────────────────────────┴─────────────────┴─────────────┘
```

### 9.2 Stratified Group Cross-Validation (`GroupKFold`)
* **Participant Grouping**: If a participant completes multiple assessments (e.g., `INITIAL` and `REASSESSMENT`), all attempts from the same `user_id` MUST remain within the same fold to avoid data leakage across train and test sets.
* **Stratification**: Folds are stratified by target class balance ($y$) to maintain equal class proportions across folds.
* **Random Seed Lock**: All random split operations fix `random_state = 42`.

---

## 10. Class Imbalance Handling Strategy

### 10.1 Diagnostic Step
Prior to model training in Phase 5B, class distribution frequencies $N_k / N$ will be computed.

### 10.2 Mitigation Techniques
* **Algorithmic Weighting**: Apply class-weight adjustments inversely proportional to class frequencies:
  $$w_k = \frac{N}{K \cdot N_k}$$
  Supported natively in LightGBM via `class_weight='balanced'`.
* **Evaluation Alignment**: Primary optimization metric is set to **Macro F1-Score** and **Balanced Accuracy** rather than raw accuracy, ensuring equal importance across minority and majority classes.

---

## 11. Model Candidates & Selection Strategy

| Model Family | Algorithm Candidate | Role & Rationale | SHAP Compatibility |
| :--- | :--- | :--- | :--- |
| **Linear Baseline** | Logistic Regression (L2 / ElasticNet) | Benchmark baseline; simple, highly interpretable linear reference | LinearSHAP |
| **Decision Tree Baseline** | Single Decision Tree (`max_depth=4`) | Non-linear baseline reference; simple rule visualization | TreeSHAP |
| **Gradient Boosting (Primary)** | **LightGBM (`LGBMClassifier`)** | **Primary Model Candidate**; optimized for tabular data, handles missing values natively, high performance | **TreeSHAP (Fast native C++ implementation)** |
| **Gradient Boosting (Secondary)** | XGBoost (`XGBClassifier`) | Secondary benchmark model for validation comparison | TreeSHAP |

*Note: LightGBM is selected as the primary candidate because of its native handling of sparse/missing tabular features and optimal performance with TreeSHAP.*

---

## 12. Evaluation Metrics & Baseline Comparison

### 12.1 Evaluation Metrics
For multiclass capability classification ($K$ classes):
1. **Macro F1-Score**: $\frac{1}{K} \sum_{k=1}^K F1_k$ (Primary overall metric).
2. **Balanced Accuracy**: Arithmetic mean of recall across each class.
3. **Precision & Recall per Class**: Evaluated via Confusion Matrix to monitor overconfidence misclassification rates.
4. **Multiclass ROC-AUC**: One-vs-Rest (OvR) macro-averaged ROC-AUC score.

### 12.2 Baseline Comparison Plan
During Phase 5B, model performance will be evaluated against two benchmarks:
1. **Zero-R / Majority Class Baseline**: Predicts the most frequent class.
2. **Phase 3 Deterministic Baseline**: Fixed threshold rule-based classification.

---

## 13. SHAP (Explainable AI) Compatibility Design

To ensure seamless integration with Phase 6 (Explainable AI), Phase 5A establishes strict compatibility requirements:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        PHASE 6 SHAP PREPARATION                             │
├─────────────────────────────────────────────────────────────────────────────┤
│ 1. Stable Feature Ordering: Feature names and order stored in JSON schema   │
│ 2. Artifact Packaging: Model, scaler, and encoder saved in unified pipeline │
│ 3. TreeSHAP Compatibility: Model candidate (LightGBM) supports TreeExplainer │
│ 4. Local Attribution Output: Map feature attributions to domain categories  │
└─────────────────────────────────────────────────────────────────────────────┘
```

1. **Feature Name Integrity**: Input columns must maintain immutable naming matching the backend feature vector keys (`cat_budgeting_pct`, `cat_saving_pct`, etc.).
2. **TreeSHAP Optimization**: LightGBM booster artifacts (`model.joblib` or `model.txt`) will be saved alongside a feature schema manifest to allow instant initialization of `shap.TreeExplainer(model)`.
3. **Attribution Contract**: Local SHAP values $\phi_i(x)$ will measure the marginal contribution of each category percentage toward increasing or decreasing the predicted risk tier.

---

## 14. Future Prediction API Contract (Phase 5B Integration)

The future Python FastAPI `ml-service` prediction endpoint (`POST /api/v1/predict`) will implement the following JSON contract:

```json
{
  "request": {
    "assessmentId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "assessmentType": "INITIAL",
    "featureVector": {
      "cat_budgeting_pct": 100.0,
      "cat_saving_pct": 50.0,
      "cat_credit_pct": null,
      "cat_investing_pct": 25.0,
      "cat_insurance_pct": null,
      "cat_taxation_pct": 0.0,
      "cat_debt_management_pct": 75.0,
      "cat_financial_planning_pct": 60.0,
      "cat_interest_pct": 100.0,
      "cat_inflation_pct": 50.0,
      "cat_risk_pct": 0.0,
      "diff_beginner_pct": 100.0,
      "diff_intermediate_pct": 50.0,
      "diff_advanced_pct": 25.0
    }
  },
  "response": {
    "prediction": "OVERCONFIDENT",
    "confidence": 0.8425,
    "probabilities": {
      "FINANCIALLY_COMPETENT": 0.0820,
      "OVERCONFIDENT": 0.8425,
      "UNDERCONFIDENT": 0.0415,
      "NAIVE_INCOMPETENT": 0.0340
    },
    "modelVersion": "lgb-v1.0.0",
    "featureSchemaVersion": "v1",
    "evaluatedAt": "2026-09-24T12:00:00Z"
  }
}
```

---

## 15. Reproducibility & Environment Control

To ensure 100% reproducible research results:
* **Python Runtime**: Python 3.11.x.
* **Random State**: Locked to seed `42` across numpy, scikit-learn, and LightGBM.
* **Dataset Hashing**: Datasets ingested for training will have their SHA-256 checksum recorded in `research/reports/data_manifest.json`.
* **Artifact Registry**: Model binaries, feature column lists, and hyperparameter configs will be versioned under `research/results/models/`.

---

## 16. Ethics & Privacy Considerations

1. **PII Minimization**: Participant names and emails are strictly excluded from dataset exports. Records are referenced solely via anonymous UUIDs (`user_id`).
2. **Sensitive Attributes**: Demographic features (such as age or occupation) are evaluated for fairness to ensure model performance is unbiased across demographic subgroups.
3. **Financial Advice Disclaimer**: Model predictions and personalized recommendations are explicitly framed as **educational assessment tools** and do not constitute certified personal financial advice.

---

## 17. Research Limitations & Risk Analysis

1. **Observational Bias**: Survey or assessment data may exhibit response bias or self-selection bias.
2. **Domain Boundaries**: Assessment questions cover specific financial categories; unassessed domains cannot be evaluated by the model.
3. **Sample Size Sensitivity**: Performance on rare capability archetypes depends heavily on empirical class representation in the final verified dataset.

---

## 18. Phase 5B Implementation Roadmap

Upon formal approval of Phase 5A and empirical verification of the target dataset:
1. **Data Ingestion & Verification**: Load verified dataset into `research/dataset/`, validate schema, verify SHA-256 checksum.
2. **Baseline Experimentation**: Train Logistic Regression and Decision Tree baselines in `research/notebooks/`.
3. **LightGBM Training**: Train LightGBM classifier with Stratified Group 5-Fold Cross-Validation and hyperparameter optimization.
4. **Model Serialization**: Export finalized model binary (`model.joblib`) and metadata manifest.
5. **API Deployment**: Integrate inference code into Python `ml-service` (`app/main.py`).
