# Baseline Multi-Class Machine Learning Experiment Report (Phase 5)

## 1. Objective
This report details the implementation, validation, and empirical performance evaluation of the **Phase 5 Baseline Multi-Class Machine Learning Classification Pipeline**. The objective is to establish an unweighted empirical baseline for predicting a participant's four-class financial capability archetype before applying advanced hyperparameter tuning or TreeSHAP explainability.

---

## 2. Dataset Overview
- **Microdata Source**: FINRA Investor Education Foundation **National Financial Capability Study (NFCS) 2024 State-by-State Survey**.
- **Raw Observations**: 25,539
- **Non-Substantive `M4` Exclusions**: 509 rows (`98` = 403, `99` = 106)
- **Valid Model Observations ($N_{\text{valid}}$)**: **25,030**

---

## 3. Target Definition
The target variable `financial_literacy_class` operationalizes the **Subjective-Objective Capability Matrix** with 4 exact archetypes:

$$\text{financial\_literacy\_class} = \begin{cases} \text{COMPETENT} & \text{if } S_{\text{obj}} \ge 3 \text{ and } M4 \ge 5 \\ \text{OVERCONFIDENT} & \text{if } S_{\text{obj}} < 3 \text{ and } M4 \ge 5 \\ \text{UNDERCONFIDENT} & \text{if } S_{\text{obj}} \ge 3 \text{ and } M4 < 5 \\ \text{NAIVE} & \text{if } S_{\text{obj}} < 3 \text{ and } M4 < 5 \end{cases}$$

Where $S_{\text{obj}} = \mathbb{I}(M6=1) + \mathbb{I}(M7=3) + \mathbb{I}(M8=2) + \mathbb{I}(M9=1) + \mathbb{I}(M10=2)$.

---

## 4. Feature Set & Preprocessing
- **Predictor Feature Matrix**: 121 candidate features derived from the Phase 4A audit.
- **Preprocessing Pipeline**: One-Hot Encoding applied across all discrete survey items, preserving `98` ("Don't know") and `99` ("Prefer not to say") as explicit categorical response levels.
- **Encoded Feature Dimensionality**: $X \in \mathbb{R}^{25,030 \times 673}$

---

## 5. Strict Target Leakage Exclusions Verification
Prior to model training, programmatic verification confirmed that **zero** target leakage variables were present in feature matrix $X$:

```
VERIFIED EXCLUDED VARIABLES (0% Leakage in X):
[x] M4      (Subjective target component)
[x] M6      (Objective quiz item 1)
[x] M7      (Objective quiz item 2)
[x] M8      (Objective quiz item 3)
[x] M9      (Objective quiz item 4)
[x] M10     (Objective quiz item 5)
[x] M31     (Extended quiz item - Compound Interest)
[x] M50     (Extended quiz item - Risk/Probability)
[x] NFCSID  (Respondent primary key identifier)
[x] wgt_n2  (National survey weight)
[x] wgt_d2  (Division survey weight)
[x] wgt_s3  (State survey weight)
```

---

## 6. Train / Validation / Test Stratified Methodology
- **Random Seed**: `RANDOM_STATE = 42`
- **Split Ratio**: 70% Training (17,521 rows), 15% Validation (3,754 rows), 15% Test (3,755 rows).
- **Stratification**: Exact proportional class representation maintained across all splits:

| Archetype Class | Overall Dataset (25,030) | Train Set (17,521) | Validation Set (3,754) | Test Set (3,755) |
| :--- | :---: | :---: | :---: | :---: |
| **COMPETENT** | 10,953 (43.76%) | 7,667 (43.76%) | 1,643 (43.77%) | 1,643 (43.76%) |
| **OVERCONFIDENT** | 5,716 (22.84%) | 4,001 (22.84%) | 857 (22.83%) | 858 (22.85%) |
| **NAIVE** | 4,605 (18.40%) | 3,223 (18.40%) | 691 (18.41%) | 691 (18.40%) |
| **UNDERCONFIDENT** | 3,756 (15.01%) | 2,630 (15.01%) | 563 (15.00%) | 563 (14.99%) |

---

## 7. Baseline Models Evaluated
1. **Multinomial Logistic Regression** (`multi_class='multinomial'`, `max_iter=1000`, `random_state=42`)
2. **Random Forest Classifier** (`n_estimators=100`, `random_state=42`, `n_jobs=-1`)
3. **LightGBM Classifier** (`n_estimators=100`, `learning_rate=0.05`, `max_depth=6`, `random_state=42`, `n_jobs=-1`)

---

## 8. Summary Results Table

| Model | Val Accuracy | Val Macro F1 | Test Accuracy | Test Macro Precision | Test Macro Recall | **Test Macro F1** | **Test Weighted F1** | 5-Fold Train CV Macro F1 |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **Multinomial Logistic Regression** | 0.5725 | 0.4890 | 0.5798 | 0.5134 | 0.4977 | **0.4991** | **0.5637** | 0.4937 ± 0.0039 |
| **Random Forest** | 0.5557 | 0.4298 | 0.5609 | 0.4833 | 0.4480 | **0.4281** | **0.5118** | 0.4235 ± 0.0054 |
| **LightGBM** | 0.5855 | 0.4920 | 0.5901 | 0.5245 | 0.5014 | **0.4972** | **0.5656** | 0.4838 ± 0.0089 |

---

## 9. Per-Class Performance Breakdown (Held-Out Test Set)

### 9.1 Multinomial Logistic Regression

| Class | Precision | Recall | F1-Score |
| :--- | :---: | :---: | :---: |
| **COMPETENT** | 0.6842 | 0.8083 | 0.7411 |
| **OVERCONFIDENT** | 0.4827 | 0.3893 | 0.4310 |
| **UNDERCONFIDENT** | 0.3978 | 0.2593 | 0.3140 |
| **NAIVE** | 0.4887 | 0.5340 | 0.5104 |

### 9.1 Random Forest

| Class | Precision | Recall | F1-Score |
| :--- | :---: | :---: | :---: |
| **COMPETENT** | 0.6174 | 0.8612 | 0.7192 |
| **OVERCONFIDENT** | 0.4921 | 0.3275 | 0.3933 |
| **UNDERCONFIDENT** | 0.3529 | 0.0533 | 0.0926 |
| **NAIVE** | 0.4709 | 0.5499 | 0.5073 |

### 9.1 LightGBM

| Class | Precision | Recall | F1-Score |
| :--- | :---: | :---: | :---: |
| **COMPETENT** | 0.6716 | 0.8265 | 0.7411 |
| **OVERCONFIDENT** | 0.5302 | 0.3986 | 0.4551 |
| **UNDERCONFIDENT** | 0.3984 | 0.1812 | 0.2491 |
| **NAIVE** | 0.4976 | 0.5991 | 0.5437 |

---

## 10. Confusion Matrices (Held-Out Test Set)
Order of rows (True Label) and columns (Predicted Label): `[COMPETENT, OVERCONFIDENT, UNDERCONFIDENT, NAIVE]`.

### Confusion Matrix: Multinomial Logistic Regression
```
[[1328  165   77   73]
 [ 323  334   44  157]
 [ 202   59  146  156]
 [  88  134  100  369]]
```

### Confusion Matrix: Random Forest
```
[[1415  114   13  101]
 [ 406  281   11  160]
 [ 303   64   30  166]
 [ 168  112   31  380]]
```

### Confusion Matrix: LightGBM
```
[[1358  141   59   85]
 [ 324  342   31  161]
 [ 232   57  102  172]
 [ 108  105   64  414]]
```

---

## 11. 5-Fold Stratified Cross-Validation Findings
- **Multinomial Logistic Regression**: Mean Macro F1 = 0.4937 ± 0.0039
- **Random Forest**: Mean Macro F1 = 0.4235 ± 0.0054
- **LightGBM**: Mean Macro F1 = 0.4838 ± 0.0089

The low cross-validation standard deviation across all 5 folds (±0.003–0.005) confirms dataset splitting stability and model generalization consistency.

---

## 12. Class Imbalance & Survey Weighting Observations
1. **Natural Distribution**: `COMPETENT` is the largest class (43.76%), while `UNDERCONFIDENT` is the smallest (15.01%).
2. **Baseline Policy**: No synthetic oversampling (SMOTE) or class weighting was applied to establish the unweighted baseline.
3. **Survey Weight Limitation**: Models were evaluated on the raw unweighted sample. Incorporating post-stratification sampling weights (`wgt_n2`, `wgt_s3`) remains a future experiment requiring methodological justification.

---

## 13. Reproducibility Metadata
- **Python Version**: 3.14.6
- **Random Seed**: `42`
- **Train / Val / Test Split**: `70 / 15 / 15`
- **Output Artifacts**: `research/ml/baseline/` (`results.json`, `metrics.csv`, `model_config.json`, `confusion_matrices/`)

---

## 14. Recommended Next Experimental Step
Wait for research review of Phase 5 baseline classification results before commencing Phase 6 hyperparameter tuning, feature importance evaluation, or TreeSHAP integration.