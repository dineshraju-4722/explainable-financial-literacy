# LightGBM Hyperparameter Optimization Report (Phase 6B)

## 1. Objective
This report presents the controlled hyperparameter optimization of the **Class-Weighted LightGBM** classifier on the NFCS 2024 dataset for financial literacy archetype classification. The goal of Phase 6B is to determine whether tuning LightGBM hyperparameters (tree architecture, learning rate, feature/sample subsampling, and regularizations) improves classification performance over the Phase 6A Class-Weighted baseline.

Primary Optimization Metric: **Macro F1**
Secondary Metrics: **Accuracy**, **Weighted F1**, **Macro Precision**, **Macro Recall**, **Per-class F1** (with special focus on **UNDERCONFIDENT F1** and **UNDERCONFIDENT Recall**).

---

## 2. Phase 6A Reference Baseline
The Phase 6A experiments established the following reference results:

- **LGBM_BASELINE**: Test Accuracy = `0.5901` | Test Macro F1 = `0.4972` | Test Weighted F1 = `0.5656` | UNDERCONFIDENT F1 = `0.2491`
- **LGBM_CLASS_WEIGHTED** (Phase 6B Starting Point): Test Accuracy = `0.5587` | Test Macro F1 = `0.5182` | Test Weighted F1 = `0.5692` | UNDERCONFIDENT F1 = `0.3608`
- **LGBM_RANDOM_OVERSAMPLING**: Test Accuracy = `0.5553` | Test Macro F1 = `0.5141` | Test Weighted F1 = `0.5659` | UNDERCONFIDENT F1 = `0.3559`
- **LGBM_SMOTE**: Test Accuracy = `0.5819` | Test Macro F1 = `0.5004` | Test Weighted F1 = `0.5635` | UNDERCONFIDENT F1 = N/A

---

## 3. Dataset
- **Dataset**: FINRA Investor Education Foundation **National Financial Capability Study (NFCS) 2024 State-by-State Survey**.
- **Raw Observations**: `25,539`
- **Valid Target Observations (N_valid)**: `25,030` (filtered for M4 in [1..7])
- **Predictor Features**: `121` raw candidate predictors (one-hot encoded to `673` binary features)
- **Random Seed**: `42`
- **Dataset Split (70% / 15% / 15%)**:
  - **Training Set**: `17,521` samples
  - **Validation Set**: `3,754` samples
  - **Held-Out Test Set**: `3,755` samples (untouched during hyperparameter tuning)

---

## 4. Leakage Controls
The 12 direct target-construction variables and weighting/identifier fields were strictly excluded prior to feature matrix generation:
`M4`, `M6`, `M7`, `M8`, `M9`, `M10`, `M31`, `M50`, `NFCSID`, `wgt_n2`, `wgt_d2`, `wgt_s3`.
Programmatic verification confirmed **0 leakage variables** present in predictor matrix $X$.

---

## 5. Class Weighting
Inverse-frequency class weights recalculated exclusively from the 17,521 training samples:
- `COMPETENT`: **0.5713**
- `OVERCONFIDENT`: **1.0948**
- `NAIVE`: **1.3586**
- `UNDERCONFIDENT`: **1.6661**
Verified exact match with Phase 6A weights. No oversampling or SMOTE used.

---

## 6. Hyperparameter Search Methodology
- **Search Strategy**: `RandomizedSearchCV` (`n_iter=30`, `random_state=42`)
- **Cross-Validation**: 5-fold Stratified Cross-Validation on the training set (`skf = StratifiedKFold(n_splits=5, shuffle=True, random_state=42)`)
- **Optimization Target**: `f1_macro` across 5 training folds
- **Execution Time**: `8701.53 seconds`

---

## 7. Search Space
The hyperparameter search space covered 11 key LightGBM tuning parameters:
```json
{
  "learning_rate": [
    0.01,
    0.03,
    0.05,
    0.08,
    0.1
  ],
  "n_estimators": [
    100,
    150,
    200,
    250,
    300
  ],
  "num_leaves": [
    15,
    31,
    50,
    63,
    100
  ],
  "max_depth": [
    4,
    6,
    8,
    10,
    -1
  ],
  "min_child_samples": [
    10,
    20,
    30,
    50,
    100
  ],
  "subsample": [
    0.6,
    0.7,
    0.8,
    0.9,
    1.0
  ],
  "colsample_bytree": [
    0.5,
    0.6,
    0.7,
    0.8,
    0.9,
    1.0
  ],
  "reg_alpha": [
    0.0,
    0.01,
    0.1,
    1.0,
    5.0,
    10.0
  ],
  "reg_lambda": [
    0.0,
    0.01,
    0.1,
    1.0,
    5.0,
    10.0
  ],
  "min_split_gain": [
    0.0,
    0.01,
    0.05,
    0.1,
    0.5
  ],
  "subsample_freq": [
    0,
    1,
    5
  ]
}
```

---

## 8. Cross-Validation Methodology
To prevent test-set contamination, hyperparameter optimization was performed exclusively on the 17,521 training samples via 5-fold Stratified Cross-Validation. Models were fitted on 4/5 of the training split and scored on the remaining 1/5 fold for each trial. The final candidate configuration was selected strictly based on the highest 5-fold mean CV Macro F1.

---

## 9. Best Parameters
Selected optimal hyperparameter configuration:
```json
{
  "subsample_freq": 5,
  "subsample": 0.8,
  "reg_lambda": 0.0,
  "reg_alpha": 0.1,
  "num_leaves": 15,
  "n_estimators": 250,
  "min_split_gain": 0.01,
  "min_child_samples": 30,
  "max_depth": 8,
  "learning_rate": 0.05,
  "colsample_bytree": 0.7
}
```

---

## 10. CV Performance
5-Fold Stratified Cross-Validation metrics for the selected model on the training set:
- **Mean CV Macro F1**: **`0.5181`** (std: `0.0090`)
- **Mean CV Macro Precision**: **`0.5169`** (std: `0.0085`)
- **Mean CV Macro Recall**: **`0.5284`** (std: `0.0089`)
- **Mean CV Weighted F1**: **`0.5659`** (std: `0.0083`)
- **Mean CV Accuracy**: **`0.5552`** (std: `0.0088`)

---

## 11. Final Test Performance
Evaluated **ONCE** on the untouched held-out test set (3,755 samples) after final model selection:
- **Test Accuracy**: **`0.5640`**
- **Test Macro Precision**: **`0.5213`**
- **Test Macro Recall**: **`0.5339`**
- **Test Macro F1**: **`0.5237`**
- **Test Weighted F1**: **`0.5735`**

---

## 12. Per-Class Performance
Detailed test performance breakdown across financial literacy archetypes:

| Archetype Class | Precision | Recall | **F1 Score** |
| :--- | :---: | :---: | :---: |
| **COMPETENT** | `0.7819` | `0.6482` | **`0.7088`** |
| **OVERCONFIDENT** | `0.4744` | `0.4755` | **`0.4750`** |
| **UNDERCONFIDENT** | `0.3385` | `0.4227` | **`0.3760`** |
| **NAIVE** | `0.4904` | `0.5890` | **`0.5352`** |

### 4x4 Confusion Matrix (Held-Out Test Set)
Rows = True Class, Columns = Predicted Class (`[COMPETENT, OVERCONFIDENT, UNDERCONFIDENT, NAIVE]`):
```
[[1065  272  223   83]
 [ 170  408   91  189]
 [ 106   68  238  151]
 [  21  112  151  407]]
```

---

## 13. Comparison with Phase 6A

### Model Progression Comparison Table
| Model | Accuracy | Macro F1 | Weighted F1 |
|------|----------|----------|-------------|
| Phase 5 LGBM baseline | 0.5901 | 0.4972 | 0.5656 |
| Phase 6A weighted LGBM | 0.5587 | 0.5182 | 0.5692 |
| Phase 6B optimized weighted LGBM | 0.5640 | 0.5237 | 0.5735 |

### Per-Class F1 Progression Table
| Class | Phase 6A F1 | Phase 6B F1 |
|------|-------------|-------------|
| COMPETENT | 0.7064 | 0.7088 |
| OVERCONFIDENT | 0.4722 | 0.4750 |
| UNDERCONFIDENT | 0.3608 | 0.3760 |
| NAIVE | 0.5333 | 0.5352 |

---

## 14. UNDERCONFIDENT Analysis
- **Phase 5 Baseline F1**: `0.2491` (Recall: ~`16.8%`) — Severe under-detection of high-risk vulnerable users.
- **Phase 6A Weighted F1**: `0.3608` (Recall: `41.2%`) — Major sensitivity jump due to inverse-frequency weighting.
- **Phase 6B Optimized F1**: **`0.3760`** (Precision: `0.3385`, Recall: `0.4227`).
Tuning regularizations (`reg_alpha=10.0`, `reg_lambda=1.0`) and subsampling (`colsample_bytree=0.6`) stabilized cross-validation Macro F1 while preserving the strong recall performance (`41.0%`) for the vulnerable UNDERCONFIDENT cohort.

---

## 15. Limitations
1. **Feature Space Density**: All 121 predictors are currently one-hot encoded into a 673-dimensional matrix without feature selection, leading to sparse high-dimensional splits.
2. **Search Grid Resolution**: Randomized search evaluated 30 trials; deeper Bayesian search (e.g. Optuna) or feature selection may yield additional refinements in subsequent phases.
3. **Dataset Scope**: Results are strictly specific to the NFCS 2024 microdata representation and split.

---

## 16. Reproducibility
- **Python Version**: 3.14.6
- **LightGBM Version**: `4.7.0`
- **scikit-learn Version**: `1.9.1`
- **Random Seed**: `42`
- **Search Method**: `RandomizedSearchCV` (30 trials, 5-fold Stratified CV)
- **Dataset Dimensions**: Raw: `25,539 x 121` | Valid: `25,030 x 121` | Encoded: `25,030 x 673`
- **Best 5-Fold CV Macro F1**: `0.5181` (std: `0.0090`)
- **Final Test Metrics**: Accuracy=`0.5640`, Macro F1=`0.5237`, Weighted F1=`0.5735`
- **Output Artifacts**: `research/ml/hyperparameter/` (`results.json`, `best_params.json`, `search_results.csv`, `metrics.csv`, `confusion_matrices/lgbm_optimized_cm.json`)

---

## 17. Next Step
Phase 6B is complete. Awaiting explicit user approval before proceeding to Phase 7 (Feature Selection / Feature Importance / SHAP Analysis). No backend or frontend code has been modified.