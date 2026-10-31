# Final Research Results Package Index (Phase 7H)

**Project Title**: Explainable Machine Learning-Based Financial Literacy Assessment and Personalized Financial Education System  
**Status**: **FINAL RESEARCH RESULTS PACKAGE: PASS** (Research frozen & authoritative)

---

## Results Package Contents

| Document File | Purpose & Contents |
| :--- | :--- |
| **[`dataset-summary.md`](./dataset-summary.md)** | Summary of NFCS 2024 microdata, sample size ($N = 25,030$), and feature processing pipeline. |
| **[`target-distribution.md`](./target-distribution.md)** | Mathematical definition of 4 archetypes and target distribution breakdown ($N = 25,030$). |
| **[`model-comparison.md`](./model-comparison.md)** | Phase 5 baseline performance table (Logistic Regression, Random Forest, LightGBM). |
| **[`imbalance-results.md`](./imbalance-results.md)** | Phase 6A class imbalance comparison (Baseline, Class-Weighted, Oversampling, SMOTE). |
| **[`hyperparameter-results.md`](./hyperparameter-results.md)** | Phase 6B LightGBM hyperparameter optimization setup, search grid, and selected parameters. |
| **[`feature-selection-results.md`](./feature-selection-results.md)** | Phase 6C-R `TOP_75` feature selection cross-validation and held-out test evaluation. |
| **[`final-model.md`](./final-model.md)** | Final locked LightGBM v1.0.0 model specification, hyperparameters, and test confusion matrix. |
| **[`shap-results.md`](./shap-results.md)** | Phase 6D TreeSHAP explainability results, global top-20 variables, category distribution, & UNDERCONFIDENT deep dive. |
| **[`limitations.md`](./limitations.md)** | 10 methodological limitations supported by empirical artifacts for publication. |
| **[`claims-guidelines.md`](./claims-guidelines.md)** | Approved safe paper wording guidelines and strictly prohibited claims. |
| **[`system-architecture.md`](./system-architecture.md)** | Full-stack Docker architecture and application/research data separation verification. |

---

## Verified Core Metrics Summary

- **Raw Dataset Observations**: `25,539`
- **Valid Model Sample Size ($N_{\text{valid}}$)**: `25,030`
- **Final Model Predictor Subset**: `75` original NFCS variables (`503` binary encoded features)
- **Model Version**: `1.0.0` (`LightGBM_TOP75_ClassWeighted`)
- **Held-Out Test Set Size**: `3,755` observations
- **Final Test Performance**:
  - Accuracy: **`0.5619`**
  - Macro Precision: **`0.5189`**
  - Macro Recall: **`0.5295`**
  - **Macro F1**: **`0.5205`**
  - **Weighted F1**: **`0.5721`**
- **Per-Class Test F1 Scores**:
  - `COMPETENT`: **`0.7079`**
  - `OVERCONFIDENT`: **`0.4910`**
  - `UNDERCONFIDENT`: **`0.3615`** (Recall: **`0.4103`**)
  - `NAIVE`: **`0.5216`**
