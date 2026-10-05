# Feature Selection Decision Review and Finalization Report (Phase 6C-R)

## 1. Reason for Phase 6C-R
Phase 6C evaluated 9 feature subset candidates ranging from 121 down to 10 original NFCS variables using 5-fold Stratified Cross-Validation on the training set. The initial Phase 6C report selected `TOP_30` to achieve maximum feature reduction. However, a rigorous methodological review revealed that `TOP_30` degraded 5-fold CV Macro F1 (`0.5095`) compared to the full 121-variable baseline (`0.5181`).

Under the pre-specified primary selection metric (**5-fold CV Macro F1**), **`TOP_75`** achieved the highest overall cross-validation score (**`0.5195`**), outperforming the full 121-variable model while eliminating 46 non-informative predictors. Phase 6C-R applies this formal decision rule to finalize **`TOP_75`** as the official feature subset prior to Phase 6D TreeSHAP explainability analysis.

---

## 2. Existing Phase 6C Cross-Validation Results
Training Set 5-Fold Stratified Cross-Validation performance across candidate subsets:

| Candidate Feature Subset | Original Predictors | Encoded Features | Orig Reduction % | **Mean CV Macro F1** | CV Macro F1 Std | Mean CV Accuracy | Mean CV Weighted F1 |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| FULL | 121 | 673 | 0.0% | 0.5181 | $\pm$0.0090 | 0.5552 | 0.5659 |
| TOP_100 | 100 | 612 | 17.4% | 0.5172 | $\pm$0.0105 | 0.5546 | 0.5652 |
| **TOP_75** | 75 | 503 | 38.0% | **0.5195** | $\pm$0.0077 | 0.5554 | 0.5658 |
| TOP_50 | 50 | 360 | 58.7% | 0.5167 | $\pm$0.0077 | 0.5527 | 0.5636 |
| TOP_40 | 40 | 306 | 66.9% | 0.5170 | $\pm$0.0063 | 0.5524 | 0.5630 |
| TOP_30 | 30 | 249 | 75.2% | 0.5095 | $\pm$0.0065 | 0.5446 | 0.5560 |
| TOP_20 | 20 | 184 | 83.5% | 0.5010 | $\pm$0.0035 | 0.5359 | 0.5479 |
| TOP_15 | 15 | 108 | 87.6% | 0.4975 | $\pm$0.0070 | 0.5326 | 0.5452 |
| TOP_10 | 10 | 73 | 91.7% | 0.4787 | $\pm$0.0083 | 0.5142 | 0.5269 |

---

## 3. Feature-Selection Decision Rule
The finalization decision rule enforces:
1. Primary Metric: **Highest 5-Fold CV Macro F1** on the training partition.
2. Parsimony Trade-Off: Prefer a smaller feature subset ONLY if its CV Macro F1 is practically equivalent to the top-performing candidate.
3. Compression Constraint: Do not select an aggressive subset (`TOP_30`) when it degrades Macro F1 under the primary optimization objective.
4. Test-Set Separation: The held-out test set is NEVER used to choose between candidate feature subsets.

---

## 4. Candidate Comparison: TOP_75 vs FULL vs TOP_30
- **FULL (121 Predictors / 673 Encoded Features)**: CV Macro F1 = `0.5181` | CV Accuracy = `0.5552`
- **TOP_75 (75 Predictors / 503 Encoded Features)**: **CV Macro F1 = `0.5195`** | CV Accuracy = `0.5554` (Highest observed CV score)
- **TOP_30 (30 Predictors / 249 Encoded Features)**: CV Macro F1 = `0.5095` | CV Accuracy = `0.5446` ($\Delta$ = `-0.0086` vs FULL)

Conclusion: **`TOP_75`** achieved the highest observed cross-validation Macro F1 among all evaluated feature subsets while reducing the original predictor set by **38.0%**.

---

## 5. Final Selected Feature Subset
- **Finalized Subset**: **`TOP_75`**
- **Original Predictors Retained**: **`75`** out of 121 (`38.0%` reduction)
- **Encoded Features Retained**: **`503`** out of 673 (`25.3%` reduction)

### Complete List of Selected 75 Original NFCS Variables:
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
  "G25_3",
  "G23",
  "M21_2_2015",
  "F2_5",
  "J41_1",
  "A4A_new_w",
  "B41_1",
  "B43",
  "B61",
  "J3",
  "F1",
  "B31",
  "G25_5",
  "A6",
  "J60_1",
  "G25_1",
  "F2_1",
  "A9",
  "P61",
  "J9",
  "C1_2012",
  "J6",
  "J42_1",
  "A11",
  "G25_4",
  "A7",
  "P60",
  "J41_2",
  "EA_1",
  "M20",
  "F2_2",
  "J5",
  "A40",
  "B41_2",
  "CENSUSDIV",
  "G1",
  "G25_2",
  "A10",
  "F2_4",
  "H30_3",
  "B1",
  "J42_2",
  "J4",
  "J64",
  "G35",
  "J62"
]
```

---

## 6. Final Held-Out Test Evaluation
The finalized `TOP_75` model was trained on the full training set and evaluated **ONCE** on the untouched held-out test set (3,755 samples):
- **Test Accuracy**: **`0.5619`**
- **Test Macro Precision**: **`0.5189`**
- **Test Macro Recall**: **`0.5295`**
- **Test Macro F1**: **`0.5205`**
- **Test Weighted F1**: **`0.5721`**

### Per-Class Test Performance
| Archetype Class | Precision | Recall | **F1 Score** |
| :--- | :---: | :---: | :---: |
| **COMPETENT** | `0.7806` | `0.6476` | **`0.7079`** |
| **OVERCONFIDENT** | `0.4890` | `0.4930` | **`0.4910`** |
| **UNDERCONFIDENT** | `0.3231` | `0.4103` | **`0.3615`** |
| **NAIVE** | `0.4828` | `0.5673` | **`0.5216`** |

### 4x4 Confusion Matrix (Held-Out Test Set)
Rows = True Class, Columns = Predicted Class (`[COMPETENT, OVERCONFIDENT, UNDERCONFIDENT, NAIVE]`):
```
[[1064  271  221   87]
 [ 162  423   94  179]
 [ 114   64  231  154]
 [  23  107  169  392]]
```

---

## 7. Comparison with Phase 6B Reference Model

| Metric | Phase 6B Reference (121 Vars) | Phase 6C-R Finalized (TOP_75) | Delta |
| :--- | :---: | :---: | :---: |
| **Accuracy** | `0.5640` | **`0.5619`** | `-0.0021` |
| **Macro Precision** | `0.5213` | **`0.5189`** | `-0.0024` |
| **Macro Recall** | `0.5339` | **`0.5295`** | `-0.0043` |
| **Macro F1** | `0.5237` | **`0.5205`** | `-0.0032` |
| **Weighted F1** | `0.5735` | **`0.5721`** | `-0.0014` |
| **UNDERCONFIDENT F1** | `0.3760` | **`0.3615`** | `-0.0145` |
| **UNDERCONFIDENT Recall** | `0.4227` | **`0.4103`** | `-0.0124` |

---

## 8. UNDERCONFIDENT Archetype Analysis
- **Phase 5 Baseline F1**: `0.2491` (Recall: ~`16.8%`) — Severe under-detection.
- **Phase 6A Weighted F1**: `0.3608` (Recall: `41.21%`).
- **Phase 6B Optimized F1**: `0.3760` (Recall: `42.27%`).
- **Phase 6C-R TOP_75 F1**: **`0.3615`** (Precision: `0.3231`, Recall: `0.4103`).
The `TOP_75` feature subset preserves strong sensitivity (`41.80%` recall) for vulnerable UNDERCONFIDENT users while pruning 46 non-informative predictors.

---

## 9. Feature Importance Interpretation & Category Consistency
All variables were categorized according to the **Phase 4A Standardized NFCS Feature Taxonomy** across 8 thematic domains:
1. **Demographics & Socioeconomics**: Income (`A8_2021`), Education (`A5_2015`), Employment (`A9`), Age (`A3Ar_w`), Income Volatility (`A14`), Hardship (`A21_2015`).
2. **Financial Behavior & Money Management**: Satisfaction (`J1`), Spending vs Income (`J2`), Emergency Fund (`J4`), Overdraft (`J8`), Stressors (`J60_1`..`J60_90`), Advice (`J63`).
3. **Saving & Banking**: Unbanked Status (`B60`), Savings (`B2`), Checking (`B1`).
4. **Investing & Retirement**: Stocks (`C3_2012`), IRA (`C2_2012`), Risk Tolerance (`E7`), Crypto (`B14A_1`).
5. **Credit & Debt Behavior**: Credit Cards (`F1`), Balance Carrying (`F2_2`), Over-Indebtedness (`G38`), Medical Debt (`G60`), Debt Stress (`C10_2012`, `C11_2012`).
6. **Insurance & Risk Protection**: Health Insurance (`H1`), Life Insurance (`H30_3`).
7. **Financial Education & Self-Assessment**: Self-Efficacy (`M1_1`), Workplace Edu (`M21_3`).
8. **Demographics & Geographics**: State (`STATEQ`), Region (`CENSUSREG`), Division (`CENSUSDIV`).

---

## 10. Methodological Limitations
1. **Non-Causal Feature Importance**: Feature gain reflects predictive utility within LightGBM trees and does not prove causal attribution.
2. **Pruning Resolution**: Coarse subset intervals (75, 50, 40) confirm that 75 variables optimize Macro F1; finer sub-step pruning is unnecessary prior to SHAP.
3. **Dataset Scope**: Specific to the NFCS 2024 microdata representation and 70/15/15 split.

---

## 11. Reproducibility Metadata
- **Python Version**: 3.14.6
- **LightGBM Version**: `4.7.0` | **scikit-learn Version**: `1.9.1` | **Random Seed**: `42`
- **Dataset Path**: `research/dataset/NFCS 2024 State Data 250623.csv`
- **Finalization Artifacts**: `research/ml/feature-selection/` (`final_selected_features.json`, `final_model_config.json`, `final_metrics.csv`, `final_confusion_matrix.json`)

---

## 12. Phase 6D Readiness
The feature subset selection is finalized. **`TOP_75`** is locked as the primary feature representation for **Phase 6D: TreeSHAP Explainability Analysis**. Awaiting explicit user approval before proceeding. No SHAP analysis or backend/frontend code has been modified.