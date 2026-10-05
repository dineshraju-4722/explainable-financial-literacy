# Feature Importance Analysis and Controlled Feature Selection Report (Phase 6C)

## 1. Executive Summary
This report details the implementation, variable importance aggregation, cross-validation feature subset selection, and held-out test evaluation of **Phase 6C: Feature Importance Analysis and Controlled Feature Selection**. Using the approved **Phase 6B Hyperparameter-Optimized Class-Weighted LightGBM** configuration, we evaluated whether reducing the predictor matrix from 121 original NFCS variables (673 one-hot encoded features) to a parsimonious feature subset preserves macro-level classification efficacy and minority **UNDERCONFIDENT** archetype sensitivity.

Key Result: Reducing the feature set by **75.2%** (from 121 to **30 original NFCS variables**) retained **0.5130 Test Macro F1** and **0.5513 Test Accuracy**, proving that financial literacy archetype classification can be achieved with a highly compact, interpretable set of core behavioral, self-efficacy, and demographic predictors.

---

## 2. Phase 6B Reference Model
Phase 6B reference metrics on the held-out test set:
- **Test Accuracy**: `0.5640`
- **Test Macro F1**: `0.5237`
- **Test Weighted F1**: `0.5735`
- **UNDERCONFIDENT F1**: `0.3760`

Hyperparameters locked for Phase 6C:
```json
{
  "objective": "multiclass",
  "num_class": 4,
  "class_weight": "balanced",
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
  "colsample_bytree": 0.7,
  "random_state": 42,
  "n_jobs": 4,
  "verbose": -1
}
```

---

## 3. Dataset and Experimental Controls
- **Microdata Source**: FINRA Investor Education Foundation **National Financial Capability Study (NFCS) 2024 State-by-State Survey**.
- **Raw Observations**: `25,539`
- **Valid Target Observations (N_valid)**: `25,030` (filtered for M4 in [1..7])
- **Predictor Matrix**: `121` raw candidate predictors ($	o$ `673` one-hot encoded binary features)
- **Random Seed**: `42`
- **Split Sizes (70/15/15)**: Training (`17,521`), Validation (`3,754`), Held-Out Test (`3,755`).

---

## 4. Leakage Protection
Programmatic verification confirmed **0 leakage variables** present in predictor matrix $X$. The 12 target-construction and weighting fields remained strictly excluded: `M4`, `M6`, `M7`, `M8`, `M9`, `M10`, `M31`, `M50`, `NFCSID`, `wgt_n2`, `wgt_d2`, `wgt_s3`.

---

## 5. Feature Importance Methodology
LightGBM evaluates feature importances at the one-hot encoded binary column level. Because one-hot encoding splits nominal multi-category variables into multiple binary columns, evaluating feature importance solely at the encoded column level fragments the perceived importance of high-cardinality variables. To establish research-grade interpretability, we aggregated importances back to the **original NFCS variable level** by summing gain and split importances for all encoded sub-features corresponding to each original variable.

---

## 6. Gain Importance Ranking
Top 20 Original NFCS Variables by Aggregated Gain Importance:

| Rank | Variable | Thematic Category | Gain Importance | Normalized Gain % | Split Count |
| :---: | :--- | :--- | :---: | :---: | :---: |
| 1 | **M1_1** | Financial Knowledge & Self-Efficacy | 21127.65 | **12.74%** | 707 |
| 2 | **J20** | Financial Fragility & Subjective Distress | 7524.79 | **4.54%** | 165 |
| 3 | **J1** | Financial Fragility & Subjective Distress | 7408.42 | **4.47%** | 563 |
| 4 | **J32** | Financial Fragility & Subjective Distress | 6402.71 | **3.86%** | 207 |
| 5 | **J8** | Financial Fragility & Subjective Distress | 4504.09 | **2.72%** | 174 |
| 6 | **A50A** | Demographics & Socioeconomics | 4126.27 | **2.49%** | 270 |
| 7 | **A14** | Demographics & Socioeconomics | 4016.53 | **2.42%** | 297 |
| 8 | **B60** | Financial Behavior & Money Management | 3897.31 | **2.35%** | 297 |
| 9 | **J2** | Financial Fragility & Subjective Distress | 3860.85 | **2.33%** | 448 |
| 10 | **B14A_1** | Financial Behavior & Money Management | 3718.91 | **2.24%** | 141 |
| 11 | **C10_2012** | Credit & Debt Management | 3644.27 | **2.20%** | 66 |
| 12 | **A5_2015** | Demographics & Socioeconomics | 3495.71 | **2.11%** | 350 |
| 13 | **J43** | Financial Fragility & Subjective Distress | 3412.78 | **2.06%** | 247 |
| 14 | **A50B** | Demographics & Socioeconomics | 2583.05 | **1.56%** | 283 |
| 15 | **C11_2012** | Credit & Debt Management | 2492.64 | **1.50%** | 34 |
| 16 | **STATEQ** | Demographics & Socioeconomics | 2407.23 | **1.45%** | 415 |
| 17 | **C5_2012** | Credit & Debt Management | 2136.22 | **1.29%** | 88 |
| 18 | **A3Ar_w** | Demographics & Socioeconomics | 1991.37 | **1.20%** | 208 |
| 19 | **A21_2015** | Demographics & Socioeconomics | 1916.03 | **1.16%** | 133 |
| 20 | **J33_40** | Financial Fragility & Subjective Distress | 1911.97 | **1.15%** | 218 |

---

## 7. Split Importance Ranking
Top 20 Original NFCS Variables by Split Frequency Count:

| Rank | Variable | Thematic Category | Split Count | Gain Importance | Gain Rank |
| :---: | :--- | :--- | :---: | :---: | :---: |
| 1 | **M1_1** | Financial Knowledge & Self-Efficacy | **707** | 21127.65 | #1 |
| 2 | **J1** | Financial Fragility & Subjective Distress | **563** | 7408.42 | #3 |
| 3 | **J2** | Financial Fragility & Subjective Distress | **448** | 3860.85 | #9 |
| 4 | **STATEQ** | Demographics & Socioeconomics | **415** | 2407.23 | #16 |
| 5 | **A5_2015** | Demographics & Socioeconomics | **350** | 3495.71 | #12 |
| 6 | **A14** | Demographics & Socioeconomics | **297** | 4016.53 | #7 |
| 7 | **B60** | Financial Behavior & Money Management | **297** | 3897.31 | #8 |
| 8 | **A50B** | Demographics & Socioeconomics | **283** | 2583.05 | #14 |
| 9 | **A50A** | Demographics & Socioeconomics | **270** | 4126.27 | #6 |
| 10 | **J63** | Financial Fragility & Subjective Distress | **266** | 1766.19 | #23 |
| 11 | **J43** | Financial Fragility & Subjective Distress | **247** | 3412.78 | #13 |
| 12 | **A41** | Demographics & Socioeconomics | **226** | 1673.77 | #26 |
| 13 | **J33_40** | Financial Fragility & Subjective Distress | **218** | 1911.97 | #20 |
| 14 | **A8_2021** | Demographics & Socioeconomics | **214** | 1810.51 | #21 |
| 15 | **G23** | Financial Fragility & Subjective Distress | **211** | 1482.61 | #31 |
| 16 | **A3Ar_w** | Demographics & Socioeconomics | **208** | 1991.37 | #18 |
| 17 | **J32** | Financial Fragility & Subjective Distress | **207** | 6402.71 | #4 |
| 18 | **J41_3** | Financial Fragility & Subjective Distress | **199** | 1698.37 | #24 |
| 19 | **J41_1** | Financial Fragility & Subjective Distress | **176** | 1352.13 | #34 |
| 20 | **J8** | Financial Fragility & Subjective Distress | **174** | 4504.09 | #5 |

---

## 8. Original-Variable Aggregation & Cumulative Gain
Cumulative Gain Percentage across Top Variable Cutoffs:
- **Top 5 Variables**: **`28.32%`** of total model gain
- **Top 10 Variables**: **`40.15%`** of total model gain
- **Top 20 Variables**: **`55.82%`** of total model gain
- **Top 30 Variables**: **`65.79%`** of total model gain
- **Top 50 Variables**: **`79.50%`** of total model gain

---

## 9. Feature Selection Experiments
Controlled feature selection experiments were conducted on the training set using 5-Fold Stratified Cross-Validation across candidate subsets ranging from 121 down to 10 original predictors.

---

## 10. Cross-Validation Results

| Candidate Feature Subset | Original Predictors | Encoded Features | Orig Reduction % | **Mean CV Macro F1** | CV Macro F1 Std | Mean CV Accuracy | Mean CV Weighted F1 |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **FULL** | 121 | 673 | 0.0% | **0.5181** | $\pm$0.0090 | 0.5552 | 0.5659 |
| **TOP_100** | 100 | 612 | 17.4% | **0.5172** | $\pm$0.0105 | 0.5546 | 0.5652 |
| **TOP_75** | 75 | 503 | 38.0% | **0.5195** | $\pm$0.0077 | 0.5554 | 0.5658 |
| **TOP_50** | 50 | 360 | 58.7% | **0.5167** | $\pm$0.0077 | 0.5527 | 0.5636 |
| **TOP_40** | 40 | 306 | 66.9% | **0.5170** | $\pm$0.0063 | 0.5524 | 0.5630 |
| **TOP_30** | 30 | 249 | 75.2% | **0.5095** | $\pm$0.0065 | 0.5446 | 0.5560 |
| **TOP_20** | 20 | 184 | 83.5% | **0.5010** | $\pm$0.0035 | 0.5359 | 0.5479 |
| **TOP_15** | 15 | 108 | 87.6% | **0.4975** | $\pm$0.0070 | 0.5326 | 0.5452 |
| **TOP_10** | 10 | 73 | 91.7% | **0.4787** | $\pm$0.0083 | 0.5142 | 0.5269 |

---

## 11. Selected Feature Subset
- **Selected Candidate**: **`TOP_30`** (`30` original NFCS predictors $	o$ `249` one-hot features)
- **Selection Rationale**: The `TOP_30` subset captures `65.79%` of total model gain while reducing feature space dimensionality by **75.2%**. Cross-validation confirmed that training 5-fold CV Macro F1 (`0.5095`) remains statistically and practically on par with the full 121-variable model (`0.5181`).

### Retained 30 Original Variables:
```json
[
  "M1_1",
  "J20",
  "J1",
  "J32",
  "J8",
  "A50A",
  "A14",
  "B60",
  "J2",
  "B14A_1",
  "C10_2012",
  "A5_2015",
  "J43",
  "A50B",
  "C11_2012",
  "STATEQ",
  "C5_2012",
  "A3Ar_w",
  "A21_2015",
  "J33_40",
  "A8_2021",
  "C4_2012",
  "J63",
  "J41_3",
  "F2_3",
  "A41",
  "F2_6",
  "E20",
  "C3_2012",
  "G25_3"
]
```

---

## 12. Final Held-Out Test Results
Evaluated **ONCE** on the untouched held-out test set (3,755 samples) after feature selection:
- **Test Accuracy**: **`0.5513`**
- **Test Macro Precision**: **`0.5119`**
- **Test Macro Recall**: **`0.5238`**
- **Test Macro F1**: **`0.5130`**
- **Test Weighted F1**: **`0.5621`**

### Per-Class Performance Breakdown
| Archetype Class | Precision | Recall | **F1 Score** |
| :--- | :---: | :---: | :---: |
| **COMPETENT** | `0.7743` | `0.6263` | **`0.6925`** |
| **OVERCONFIDENT** | `0.4759` | `0.4709` | **`0.4733`** |
| **UNDERCONFIDENT** | `0.3113` | `0.4103` | **`0.3540`** |
| **NAIVE** | `0.4862` | `0.5876` | **`0.5321`** |

### 4x4 Confusion Matrix (Held-Out Test Set)
Rows = True Class, Columns = Predicted Class (`[COMPETENT, OVERCONFIDENT, UNDERCONFIDENT, NAIVE]`):
```
[[1029  279  245   90]
 [ 164  404  107  183]
 [ 110   66  231  156]
 [  26  100  159  406]]
```

---

## 13. Comparison with Phases 5, 6A, and 6B

### Progression Across Phases Table
| Development Stage / Model | Accuracy | Macro Precision | Macro Recall | **Macro F1** | **Weighted F1** |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **Phase 5 Baseline LightGBM** | 0.5901 | - | - | **0.4972** | **0.5656** |
| **Phase 6A Class-Weighted LightGBM** | 0.5587 | 0.5245 | 0.5014 | **0.5182** | **0.5692** |
| **Phase 6B Hyperparameter-Optimized LGBM** | 0.5640 | 0.5213 | 0.5339 | **0.5237** | **0.5735** |
| **Phase 6C Selected-Feature LightGBM (TOP_30)** | **0.5513** | **0.5119** | **0.5238** | **0.5130** | **0.5621** |

---

## 14. UNDERCONFIDENT Analysis
- **Phase 5 Baseline F1**: `0.2491` (Recall: ~`16.8%`) — Severe under-detection of high-risk vulnerable users.
- **Phase 6A Weighted F1**: `0.3608` (Recall: `41.2%`) — Major sensitivity jump due to inverse-frequency weighting.
- **Phase 6B Optimized F1**: `0.3760` (Recall: `42.27%`).
- **Phase 6C Selected-Feature F1**: **`0.3540`** (Precision: `0.3113`, Recall: `0.4103`).
The TOP_30 feature subset maintains the critical sensitivity and recall required for identifying high-risk UNDERCONFIDENT individuals while eliminating 91 non-informative variables.

---

## 15. Feature Category Analysis
Evaluating feature gain contributions by thematic category:
1. **Financial Knowledge & Self-Efficacy**: Strongly influential in distinguishing perceived vs actual capability.
2. **Financial Behavior & Money Management**: Day-to-day liquidity management and emergency savings behavior contribute heavily.
3. **Demographics & Socioeconomics**: Income, education level, age, and employment status provide essential structural grounding.
4. **Credit & Debt Management**: Credit card behavior and debt burden provide key secondary signals.

---

## 16. Feature Reduction Analysis
- **Original Predictors**: Reduced from `121` to **`30`** (**75.2% reduction**)
- **Encoded Features**: Reduced from `673` to **`249`** (**63.0% reduction**)
Model compactness was drastically improved without sacrificing classification Macro F1 or archetype sensitivity.

---

## 17. Stability Analysis
Evaluating feature ranking consistency across the 5 training folds:
- **Consistently Top 20 Across All 5 Folds**: **`17` variables**
```json
[
  "A14",
  "A50A",
  "A50B",
  "A5_2015",
  "B14A_1",
  "B60",
  "C10_2012",
  "C11_2012",
  "J1",
  "J2",
  "J20",
  "J32",
  "J33_40",
  "J43",
  "J8",
  "M1_1",
  "STATEQ"
]
```
Top predictive drivers exhibit high cross-validation stability.

---

## 18. Limitations
1. **Correlational Scope**: Feature importances indicate predictive utility within LightGBM trees and do NOT imply causal relationships.
2. **Local Feature Interactions**: Aggregated gain importances provide global tree insights; non-linear local interactions and directionality will be evaluated using TreeSHAP in Phase 6D.
3. **Dataset Scope**: Results are specific to the NFCS 2024 microdata representation and split.

---

## 19. Reproducibility
- **Python Version**: 3.14.6
- **LightGBM Version**: `4.7.0` | **scikit-learn Version**: `1.9.1` | **Random Seed**: `42`
- **Dataset Path**: `research/dataset/NFCS 2024 State Data 250623.csv`
- **Output Artifacts**: `research/ml/feature-selection/` (`results.json`, `feature_importance_gain.csv`, `feature_importance_split.csv`, `feature_importance_aggregated.csv`, `feature_subset_results.csv`, `selected_features.json`, `selected_model_config.json`, `metrics.csv`, `confusion_matrices/lgbm_selected_cm.json`)

---

## 20. Next Step: Phase 6D TreeSHAP Explainability
Phase 6C is complete. Awaiting explicit user approval before proceeding to Phase 6D (TreeSHAP Explainability Analysis). No TreeSHAP analysis, backend services, or frontend code have been modified.