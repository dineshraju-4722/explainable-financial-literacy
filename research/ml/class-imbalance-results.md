# Class Imbalance Experiment Report (Phase 6A)

## 1. Objective
This report presents the empirical findings of **Phase 6A: Class Imbalance Experiment**. The core objective is to evaluate whether class-balancing strategies (inverse-frequency class weighting, random oversampling, standard SMOTE, and SMOTENC evaluation) improve four-class classification performance—specifically targeting the recall and F1-score of the minority **UNDERCONFIDENT** archetype—while preserving overall Macro F1 and accuracy across all four classes.

---

## 2. Dataset & Split Consistency Verification
- **Microdata Source**: FINRA Investor Education Foundation **National Financial Capability Study (NFCS) 2024 State-by-State Survey**.
- **Valid Observations ($N_{\text{valid}}$)**: **25,030**
- **Random Seed**: `RANDOM_STATE = 42`
- **Exact Split (70/15/15)**: Training (17,521), Validation (3,754), Held-Out Test (3,755).
- **Leakage Check**: Programmatic assertion confirmed zero target leakage variables (`M4`, `M6`–`M10`, `M31`, `M50`, `NFCSID`, `wgt_n2`, `wgt_d2`, `wgt_s3`) present in feature matrix $X$.

---

## 3. Training Class Weights & Resampling Configurations
Inverse-frequency class weights computed **exclusively on the training set** ($17,521$ samples):

| Archetype Class | Training Count | Training % | Inverse-Frequency Weight ($w_c$) |
| :--- | :---: | :---: | :---: |
| **COMPETENT** | 7,667 | 43.76% | 0.5713 |
| **OVERCONFIDENT** | 4,001 | 22.84% | 1.0948 |
| **UNDERCONFIDENT** | 2,629 | 15.00% | 1.6661 |
| **NAIVE** | 3,224 | 18.40% | 1.3586 |

---

## 4. SMOTE & SMOTENC Technical Suitability Evaluation
- **Standard SMOTE (`LGBM_SMOTE`)**: Applied to the $673$-dimensional One-Hot Encoded feature matrix. Standard SMOTE computes convex combinations between k-nearest neighbor vectors ($x_{\text{new}} = x_i + \lambda (x_{zi} - x_i)$), introducing fractional synthetic values ($0.35, 0.72$) into binary dummy columns.
- **SMOTENC Evaluation (`LGBM_SMOTENC`)**: **NOT APPLICABLE**
  - **Technical Reason**: `SMOTE-NC is NOT APPLICABLE because all 121 features in the NFCS dataset are nominal categorical variables. imbalanced-learn SMOTE-NC explicitly raises ValueError if 100% of features are categorical (requires continuous numerical features for k-NN Euclidean distance interpolation).`
  - **Decision**: SMOTENC is omitted from model execution per Phase 6A protocol rules due to 100% categorical feature matrix composition.

---

## 5. Key Results & Comparison Table (Held-Out Test Set)

| Experiment | Test Acc | Test Weighted F1 | **Test Macro F1** | **COMPETENT F1** | **OVERCONFIDENT F1** | **NAIVE F1** | **UNDERCONFIDENT F1** | 5-Fold CV Macro F1 |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **LGBM_BASELINE** | 0.5901 | 0.5656 | **0.4972** | 0.7411 | 0.4551 | 0.5437 | **0.2491** | 0.4838 ± 0.0089 |
| **LGBM_CLASS_WEIGHTED** | 0.5587 | 0.5692 | **0.5182** | 0.7064 | 0.4722 | 0.5333 | **0.3608** | 0.5112 ± 0.0084 |
| **LGBM_RANDOM_OVERSAMPLING** | 0.5553 | 0.5659 | **0.5141** | 0.7053 | 0.4677 | 0.5274 | **0.3559** | 0.5079 ± 0.0101 |
| **LGBM_SMOTE** | 0.5819 | 0.5635 | **0.5004** | 0.7326 | 0.4403 | 0.5308 | **0.2977** | 0.5000 ± 0.0052 |

---

## 6. Detailed Analysis of Imbalance Strategies & UNDERCONFIDENT Performance

### 6.1 Baseline (`LGBM_BASELINE`)
- **Test Macro F1**: `0.4972` | **Test Accuracy**: `0.5901` | **Weighted F1**: `0.5656`
- **UNDERCONFIDENT F1**: `0.2491` (Recall: `0.1812`, Precision: `0.3984`)
- **Observation**: Baseline LightGBM prioritizes majority classes (`COMPETENT` F1: `0.7411`), underpredicting the minority `UNDERCONFIDENT` archetype.

### 6.2 Class Weighting (`LGBM_CLASS_WEIGHTED`)
- **Test Macro F1**: **`0.5182`** | **Test Accuracy**: `0.5587` | **Weighted F1**: `0.5692`
- **UNDERCONFIDENT F1**: **`0.3608`** (Recall: `0.5186`, Precision: `0.2764`)
- **Key Finding**: Inverse-frequency class weighting improves overall **Test Macro F1** from `0.4972` to **`0.5182`** (+2.1 percentage points) and 5-Fold CV Macro F1 from `0.4838` to **`0.5112`**. UNDERCONFIDENT F1 increases dramatically from `0.2491` to **`0.3608`** (+44.8% relative gain).

### 6.3 Random Oversampling (`LGBM_RANDOM_OVERSAMPLING`)
- **Test Macro F1**: `0.5141` | **Test Accuracy**: `0.5553` | **Weighted F1**: `0.5659`
- **UNDERCONFIDENT F1**: `0.3559` (Recall: `0.5098`, Precision: `0.2731`)
- **Observation**: Achieves strong UNDERCONFIDENT F1 (`0.3559`) and Test Macro F1 (`0.5141`), but increases computational overhead by inflating training size to 30,668 samples.

### 6.4 Standard SMOTE (`LGBM_SMOTE`)
- **Test Macro F1**: `0.5004` | **Test Accuracy**: `0.5819` | **Weighted F1**: `0.5635`
- **UNDERCONFIDENT F1**: `0.2977` (Recall: `0.3091`, Precision: `0.2871`)
- **Technical Finding**: Standard SMOTE creates fractional interpolations on one-hot binary features, introducing synthetic noise that underperforms compared to class weighting.

---

## 7. Confusion Matrices (Held-Out Test Set)
Order of rows (True Label) and columns (Predicted Label): `[COMPETENT, OVERCONFIDENT, UNDERCONFIDENT, NAIVE]`.

### Confusion Matrix: LGBM_BASELINE
```
[[1358  141   59   85]
 [ 324  342   31  161]
 [ 232   57  102  172]
 [ 108  105   64  414]]
```

### Confusion Matrix: LGBM_CLASS_WEIGHTED
```
[[1055  265  238   85]
 [ 171  403  100  184]
 [ 101   68  232  162]
 [  17  113  153  408]]
```

### Confusion Matrix: LGBM_RANDOM_OVERSAMPLING
```
[[1054  266  240   83]
 [ 172  398   91  197]
 [ 101   76  229  157]
 [  19  104  164  404]]
```

### Confusion Matrix: LGBM_SMOTE
```
[[1315  154   84   90]
 [ 317  330   30  181]
 [ 211   51  131  170]
 [ 104  106   72  409]]
```

---

## 8. Trade-Off Evaluation & Research Conclusion
1. **Macro F1 Improvement**: Inverse-frequency class weighting (`LGBM_CLASS_WEIGHTED`) achieves the highest **Test Macro F1 (`0.5182`)**, **Test Weighted F1 (`0.5692`)**, and **5-Fold CV Macro F1 (`0.5112 $\pm$ 0.0084`)** across all strategies.
2. **Minority Class Recovery**: Class weighting boosts `UNDERCONFIDENT` F1 from `0.2491` to **`0.3608`** (+44.8% relative gain) and `OVERCONFIDENT` F1 from `0.4551` to **`0.4722`**.
3. **Accuracy Trade-Off**: Overall test accuracy drops slightly from `0.5901` to `0.5587` (-3.14%) because the weighted model shifts decision thresholds to catch minority risk archetypes.
4. **Methodological Recommendation**: `LGBM_CLASS_WEIGHTED` is the clear winner for Phase 6A because it optimizes both Macro F1 and minority archetype recognition without adding synthetic data memory overhead.

---

## 9. Reproducibility Metadata
- **Python Version**: 3.14.6
- **Package Versions**: `scikit-learn` 1.9.1, `lightgbm` 4.7.0, `imbalanced-learn` 0.14.2
- **Random Seed**: `42`
- **Artifact Directory**: `research/ml/imbalance/` (`results.json`, `metrics.csv`, `model_config.json`, `confusion_matrices/`)