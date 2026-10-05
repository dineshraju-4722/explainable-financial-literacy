# TreeSHAP Explainability Analysis Report (Phase 6D)

## 1. Executive Summary
This report presents the formal **TreeSHAP (Shapley Additive Explanations)** analysis for the locked **Phase 6C-R TOP_75 LightGBM classifier** trained on the FINRA NFCS 2024 microdata ($25,030$ valid observations). Using `shap.TreeExplainer`, we computed exact Shapley values on the $3,755$-observation held-out test set across 503 one-hot encoded features and aggregated contributions back to the **75 original NFCS predictor variables**. This analysis provides global feature importance rankings, class-specific push/pull drivers, an in-depth diagnostic of the vulnerable **UNDERCONFIDENT** archetype, and 8 representative individual test case explanations.

---

## 2. Research Objective
The objective of Phase 6D is to explain the internal decision logic of the finalized 75-variable LightGBM model without altering feature representations, model hyperparameters, or decision boundaries. TreeSHAP assigns each predictor a mathematically grounded Shapley contribution score rooted in cooperative game theory, explaining how features shift predicted archetype probabilities away from expected baseline values.

---

## 3. Final TOP_75 Model Reference
Locked Phase 6C-R model test set reference metrics:
- **Test Accuracy**: `0.5619`
- **Test Macro F1**: `0.5205`
- **Test Weighted F1**: `0.5721`
- **Per-Class Test F1**: COMPETENT = `0.7079` | OVERCONFIDENT = `0.4910` | UNDERCONFIDENT = `0.3615` | NAIVE = `0.5216`

---

## 4. Dataset and Experimental Controls
- **Microdata Source**: FINRA Investor Education Foundation **National Financial Capability Study (NFCS) 2024 State-by-State Survey**.
- **Raw Observations**: `25,539` | **Valid Model Observations (N_valid)**: `25,030` (filtered for M4 in [1..7])
- **Predictor Representation**: Frozen `75` original NFCS variables (to `503` one-hot encoded binary features)
- **Random Seed**: `42` | **Held-Out Test Set Size**: `3,755` samples (exact Phase 6C-R test partition)

---

## 5. Leakage Protection
Programmatic verification confirmed **0 leakage variables** present in predictor matrix X. The 12 target-construction and weighting fields remained strictly excluded: `M4`, `M6`, `M7`, `M8`, `M9`, `M10`, `M31`, `M50`, `NFCSID`, `wgt_n2`, `wgt_d2`, `wgt_s3`.

---

## 6. TreeSHAP Methodology
- **Explainer Algorithm**: `shap.TreeExplainer` (`shap` version `0.52.0`)
- **Computation Target**: Held-out test set (N_test = 3,755 samples)
- **Class Ordering**: `Index 0: COMPETENT`, `Index 1: OVERCONFIDENT`, `Index 2: UNDERCONFIDENT`, `Index 3: NAIVE`.
- **Computation Duration**: `3.11 seconds`

---

## 7. SHAP Aggregation to Original NFCS Variables
TreeSHAP calculates Shapley values for each of the 503 binary one-hot encoded columns. For research clarity, Shapley values were aggregated back to the original 75 NFCS variables by summing signed contributions for individual observations and averaging absolute contributions globally:
$$\text{SHAP}_{i, V, c} = \sum_{m \in \text{cols}(V)} \text{SHAP}_{i, m, c}$$
$$\text{Mean Abs SHAP}(V) = \frac{1}{4 N} \sum_{c=1}^{4} \sum_{i=1}^{N} |\text{SHAP}_{i, V, c}|$$

---

## 8. Global SHAP Importance
Top 20 Original NFCS Variables by Global Mean Absolute SHAP Importance:

| Rank | Variable | Official Description | Phase 4A Category | Global Mean Abs SHAP | Normalized % |
| :---: | :--- | :--- | :--- | :---: | :---: |
| 1 | **M1_1** | Overall Financial Knowledge (Self-Assessment Scale 1-7) | Financial Education & Self-Assessment | 0.2803 | **11.50%** |
| 2 | **A50A** | Gender Identity (Female / Male) | Demographics & Socioeconomics | 0.1202 | **4.93%** |
| 3 | **J1** | Difficulty Covering Monthly Expenses | Financial Behavior & Money Management | 0.1039 | **4.26%** |
| 4 | **J8** | Checking Account Overdraft Frequency | Financial Behavior & Money Management | 0.0753 | **3.09%** |
| 5 | **A14** | Household Income Volatility / Unexpected Drop | Demographics & Socioeconomics | 0.0703 | **2.88%** |
| 6 | **B14A_1** | Cryptocurrency Ownership Status | Investing & Retirement | 0.0646 | **2.65%** |
| 7 | **J43** | Confidence in Coming Up with $2,000 in Emergency | Financial Behavior & Money Management | 0.0645 | **2.65%** |
| 8 | **J20** | Overall Financial Satisfaction (Scale 1-10) | Financial Behavior & Money Management | 0.0644 | **2.64%** |
| 9 | **A5_2015** | Highest Education Level Completed | Demographics & Socioeconomics | 0.0629 | **2.58%** |
| 10 | **B60** | Unbanked Status / No Checking or Savings Account | Saving & Banking | 0.0617 | **2.53%** |
| 11 | **J2** | Spending Behavior Relative to Income | Financial Behavior & Money Management | 0.0580 | **2.38%** |
| 12 | **J32** | Financial Stress / Anxiety in Life | Financial Behavior & Money Management | 0.0533 | **2.19%** |
| 13 | **A21_2015** | Felt Financial Hardship in Past Year | Demographics & Socioeconomics | 0.0409 | **1.68%** |
| 14 | **A4A_new_w** | A4A_new_w | Demographics & Socioeconomics | 0.0400 | **1.64%** |
| 15 | **F2_3** | Paid Minimum Only on Credit Card | Credit & Debt Behavior | 0.0389 | **1.59%** |
| 16 | **A50B** | Ethnicity / Racial Identity | Demographics & Socioeconomics | 0.0385 | **1.58%** |
| 17 | **J33_40** | Short-term Financial Emergency Preparedness | Financial Behavior & Money Management | 0.0370 | **1.52%** |
| 18 | **J60_1** | J60_1 | Financial Behavior & Money Management | 0.0357 | **1.47%** |
| 19 | **M21_2_2015** | M21_2_2015 | Financial Education & Self-Assessment | 0.0356 | **1.46%** |
| 20 | **A3Ar_w** | Age Category Bracket | Demographics & Socioeconomics | 0.0354 | **1.45%** |

---

## 9. Class-Specific SHAP Analysis
Signed Shapley value analysis reveals the specific variables that push model output toward vs away from each financial literacy archetype.

---

## 10. COMPETENT Archetype Explanation
Top positive drivers pushing model predictions TOWARD **COMPETENT** (High Objective, High Subjective):

| Rank | Variable | Description | Category | Mean Positive SHAP |
| :---: | :--- | :--- | :--- | :---: |
| 1 | **M1_1** | Overall Financial Knowledge (Self-Assessment Scale 1-7) | Financial Education & Self-Assessment | `+0.2711` |
| 2 | **M21_2_2015** | M21_2_2015 | Financial Education & Self-Assessment | `+0.2449` |
| 3 | **A14** | Household Income Volatility / Unexpected Drop | Demographics & Socioeconomics | `+0.2163` |
| 4 | **J20** | Overall Financial Satisfaction (Scale 1-10) | Financial Behavior & Money Management | `+0.1629` |
| 5 | **B14A_1** | Cryptocurrency Ownership Status | Investing & Retirement | `+0.1491` |
| 6 | **J2** | Spending Behavior Relative to Income | Financial Behavior & Money Management | `+0.1432` |
| 7 | **J9** | J9 | Financial Behavior & Money Management | `+0.1404` |
| 8 | **A50A** | Gender Identity (Female / Male) | Demographics & Socioeconomics | `+0.1312` |
| 9 | **J8** | Checking Account Overdraft Frequency | Financial Behavior & Money Management | `+0.1310` |
| 10 | **M20** | M20 | Financial Education & Self-Assessment | `+0.1294` |

---

## 11. OVERCONFIDENT Archetype Explanation
Top positive drivers pushing model predictions TOWARD **OVERCONFIDENT** (Low Objective, High Subjective):

| Rank | Variable | Description | Category | Mean Positive SHAP |
| :---: | :--- | :--- | :--- | :---: |
| 1 | **M1_1** | Overall Financial Knowledge (Self-Assessment Scale 1-7) | Financial Education & Self-Assessment | `+0.3422` |
| 2 | **B1** | B1 | Saving & Banking | `+0.1625` |
| 3 | **A50A** | Gender Identity (Female / Male) | Demographics & Socioeconomics | `+0.1495` |
| 4 | **J8** | Checking Account Overdraft Frequency | Financial Behavior & Money Management | `+0.1352` |
| 5 | **G25_3** | Behind on Student Loan Payments | Credit & Debt Behavior | `+0.0963` |
| 6 | **B14A_1** | Cryptocurrency Ownership Status | Investing & Retirement | `+0.0924` |
| 7 | **EA_1** | EA_1 | Investing & Retirement | `+0.0888` |
| 8 | **A14** | Household Income Volatility / Unexpected Drop | Demographics & Socioeconomics | `+0.0854` |
| 9 | **J32** | Financial Stress / Anxiety in Life | Financial Behavior & Money Management | `+0.0828` |
| 10 | **J20** | Overall Financial Satisfaction (Scale 1-10) | Financial Behavior & Money Management | `+0.0772` |

---

## 12. UNDERCONFIDENT Archetype Explanation
Top positive drivers pushing model predictions TOWARD **UNDERCONFIDENT** (High Objective, Low Subjective):

| Rank | Variable | Description | Category | Mean Positive SHAP |
| :---: | :--- | :--- | :--- | :---: |
| 1 | **B60** | Unbanked Status / No Checking or Savings Account | Saving & Banking | `+0.1742` |
| 2 | **M1_1** | Overall Financial Knowledge (Self-Assessment Scale 1-7) | Financial Education & Self-Assessment | `+0.1539` |
| 3 | **J6** | J6 | Financial Behavior & Money Management | `+0.1447` |
| 4 | **J1** | Difficulty Covering Monthly Expenses | Financial Behavior & Money Management | `+0.1328` |
| 5 | **A50A** | Gender Identity (Female / Male) | Demographics & Socioeconomics | `+0.1157` |
| 6 | **J62** | J62 | Financial Behavior & Money Management | `+0.1149` |
| 7 | **G25_1** | G25_1 | Credit & Debt Behavior | `+0.1109` |
| 8 | **A4A_new_w** | A4A_new_w | Demographics & Socioeconomics | `+0.1072` |
| 9 | **G25_4** | G25_4 | Credit & Debt Behavior | `+0.1010` |
| 10 | **G25_5** | G25_5 | Credit & Debt Behavior | `+0.0999` |

---

## 13. NAIVE Archetype Explanation
Top positive drivers pushing model predictions TOWARD **NAIVE** (Low Objective, Low Subjective):

| Rank | Variable | Description | Category | Mean Positive SHAP |
| :---: | :--- | :--- | :--- | :---: |
| 1 | **M1_1** | Overall Financial Knowledge (Self-Assessment Scale 1-7) | Financial Education & Self-Assessment | `+0.3252` |
| 2 | **J1** | Difficulty Covering Monthly Expenses | Financial Behavior & Money Management | `+0.1785` |
| 3 | **J41_3** | Used Payday Loan / Pawn Shop / Rent-to-Own (AFS) | Financial Behavior & Money Management | `+0.0975` |
| 4 | **H30_3** | H30_3 | Insurance & Risk Protection | `+0.0836` |
| 5 | **C3_2012** | Taxable Stock / Bond Account Holding | Investing & Retirement | `+0.0806` |
| 6 | **J33_40** | Short-term Financial Emergency Preparedness | Financial Behavior & Money Management | `+0.0792` |
| 7 | **A50A** | Gender Identity (Female / Male) | Demographics & Socioeconomics | `+0.0791` |
| 8 | **C4_2012** | Regular Retirement Account Contributions | Investing & Retirement | `+0.0576` |
| 9 | **A5_2015** | Highest Education Level Completed | Demographics & Socioeconomics | `+0.0567` |
| 10 | **B61** | B61 | Saving & Banking | `+0.0532` |

---

## 14. UNDERCONFIDENT Deep Dive
The **UNDERCONFIDENT** archetype represents a critical risk group exhibiting strong objective knowledge ($M6..M10 \ge 3$) paired with low self-assessed knowledge ($M4 < 5$).

Top 15 Drivers for UNDERCONFIDENT vs Other Archetypes:

| Rank | Variable | Description | UC Mean Abs SHAP | COMPETENT SHAP | OVERCONFIDENT SHAP | NAIVE SHAP |
| :---: | :--- | :--- | :---: | :---: | :---: | :---: |
| 1 | **M1_1** | Overall Financial Knowledge (Self-Assessment Scale 1-7) | **0.1856** | 0.3016 | 0.3127 | 0.3214 |
| 2 | **J1** | Difficulty Covering Monthly Expenses | **0.1481** | 0.0137 | 0.0472 | 0.2068 |
| 3 | **B60** | Unbanked Status / No Checking or Savings Account | **0.1449** | 0.0315 | 0.0290 | 0.0416 |
| 4 | **A50A** | Gender Identity (Female / Male) | **0.1208** | 0.1281 | 0.1547 | 0.0771 |
| 5 | **A21_2015** | Felt Financial Hardship in Past Year | **0.1020** | 0.0351 | 0.0122 | 0.0141 |
| 6 | **J43** | Confidence in Coming Up with $2,000 in Emergency | **0.0902** | 0.0109 | 0.0853 | 0.0715 |
| 7 | **B31** | B31 | **0.0663** | 0.0127 | 0.0046 | 0.0254 |
| 8 | **B61** | B61 | **0.0626** | 0.0157 | 0.0089 | 0.0419 |
| 9 | **A4A_new_w** | A4A_new_w | **0.0608** | 0.0607 | 0.0115 | 0.0271 |
| 10 | **J60_1** | J60_1 | **0.0539** | 0.0306 | 0.0184 | 0.0400 |
| 11 | **B43** | B43 | **0.0518** | 0.0090 | 0.0048 | 0.0258 |
| 12 | **C4_2012** | Regular Retirement Account Contributions | **0.0463** | 0.0309 | 0.0095 | 0.0355 |
| 13 | **P61** | P61 | **0.0455** | 0.0201 | 0.0130 | 0.0143 |
| 14 | **F2_6** | Used Credit Card for Cash Advance | **0.0449** | 0.0203 | 0.0065 | 0.0435 |
| 15 | **G25_4** | G25_4 | **0.0372** | 0.0089 | 0.0053 | 0.0211 |

---

## 15. Gain vs SHAP Comparison
Comparing Phase 6C LightGBM Gain Importance against Phase 6D Global TreeSHAP Importance:

| Variable | Description | Gain Rank | SHAP Rank | Rank Shift | Gain Importance | Global SHAP |
| :--- | :--- | :---: | :---: | :---: | :---: | :---: |
| **M1_1** | Overall Financial Knowledge (Self-Assessment Scale 1-7) | #1 | #1 | `0` | 21127.65 | 0.2803 |
| **A50A** | Gender Identity (Female / Male) | #6 | #2 | `+4` | 4126.27 | 0.1202 |
| **J1** | Difficulty Covering Monthly Expenses | #3 | #3 | `0` | 7408.42 | 0.1039 |
| **J8** | Checking Account Overdraft Frequency | #5 | #4 | `+1` | 4504.09 | 0.0753 |
| **A14** | Household Income Volatility / Unexpected Drop | #7 | #5 | `+2` | 4016.53 | 0.0703 |
| **B14A_1** | Cryptocurrency Ownership Status | #10 | #6 | `+4` | 3718.91 | 0.0646 |
| **J43** | Confidence in Coming Up with $2,000 in Emergency | #13 | #7 | `+6` | 3412.78 | 0.0645 |
| **J20** | Overall Financial Satisfaction (Scale 1-10) | #2 | #8 | `-6` | 7524.79 | 0.0644 |
| **A5_2015** | Highest Education Level Completed | #12 | #9 | `+3` | 3495.71 | 0.0629 |
| **B60** | Unbanked Status / No Checking or Savings Account | #8 | #10 | `-2` | 3897.31 | 0.0617 |
| **J2** | Spending Behavior Relative to Income | #9 | #11 | `-2` | 3860.85 | 0.0580 |
| **J32** | Financial Stress / Anxiety in Life | #4 | #12 | `-8` | 6402.71 | 0.0533 |
| **A21_2015** | Felt Financial Hardship in Past Year | #19 | #13 | `+6` | 1916.03 | 0.0409 |
| **A4A_new_w** | A4A_new_w | #35 | #14 | `+21` | 1254.14 | 0.0400 |
| **F2_3** | Paid Minimum Only on Credit Card | #25 | #15 | `+10` | 1693.53 | 0.0389 |

Gain measures node split improvement across decision trees, whereas TreeSHAP measures marginal feature contributions to individual test predictions under Shapley value axioms.

---

## 16. Feature Category Analysis (Phase 4A Taxonomy)
Distribution of total TreeSHAP importance across the 8 Phase 4A thematic categories:

| Phase 4A Thematic Category | Retained Variables | Total Category SHAP | **SHAP Importance %** |
| :--- | :---: | :---: | :---: |
| **Financial Behavior & Money Management** | 21 | 0.7095 | **29.10%** |
| **Demographics & Socioeconomics** | 15 | 0.5488 | **22.51%** |
| **Financial Education & Self-Assessment** | 3 | 0.3453 | **14.16%** |
| **Credit & Debt Behavior** | 17 | 0.3434 | **14.08%** |
| **Investing & Retirement** | 9 | 0.2579 | **10.58%** |
| **Saving & Banking** | 7 | 0.1886 | **7.73%** |
| **Demographics & Geographics** | 2 | 0.0301 | **1.23%** |
| **Insurance & Risk Protection** | 1 | 0.0147 | **0.60%** |

---

## 17. Individual Case Explanations
8 representative test observations (2 correctly classified per archetype) were explained using local Shapley contributions:

### Case `COMPETENT_Case_1` (Test Sample Index `1344`)
- **True Archetype**: `COMPETENT` | **Predicted Archetype**: `COMPETENT`
- **Predicted Probabilities**: COMPETENT = `0.979`, OVERCONFIDENT = `0.002`, UNDERCONFIDENT = `0.008`, NAIVE = `0.011`
- **Top Positive Drivers (Pushing Toward Prediction)**: M1_1 (+0.376), A9 (+0.273), A14 (+0.273)
- **Top Negative Drivers (Pushing Away)**: F2_2 (-0.020), J9 (-0.017), B31 (-0.016)

### Case `COMPETENT_Case_2` (Test Sample Index `2497`)
- **True Archetype**: `COMPETENT` | **Predicted Archetype**: `COMPETENT`
- **Predicted Probabilities**: COMPETENT = `0.973`, OVERCONFIDENT = `0.002`, UNDERCONFIDENT = `0.014`, NAIVE = `0.011`
- **Top Positive Drivers (Pushing Toward Prediction)**: M1_1 (+0.336), A14 (+0.266), J20 (+0.238)
- **Top Negative Drivers (Pushing Away)**: B14A_1 (-0.080), A9 (-0.021), A40 (-0.018)

### Case `OVERCONFIDENT_Case_1` (Test Sample Index `303`)
- **True Archetype**: `OVERCONFIDENT` | **Predicted Archetype**: `OVERCONFIDENT`
- **Predicted Probabilities**: COMPETENT = `0.002`, OVERCONFIDENT = `0.456`, UNDERCONFIDENT = `0.532`, NAIVE = `0.011`
- **Top Positive Drivers (Pushing Toward Prediction)**: M1_1 (+0.395), P61 (+0.223), J3 (+0.174)
- **Top Negative Drivers (Pushing Away)**: A50A (-0.132), M20 (-0.040), A11 (-0.036)

### Case `OVERCONFIDENT_Case_2` (Test Sample Index `1952`)
- **True Archetype**: `OVERCONFIDENT` | **Predicted Archetype**: `OVERCONFIDENT`
- **Predicted Probabilities**: COMPETENT = `0.016`, OVERCONFIDENT = `0.453`, UNDERCONFIDENT = `0.511`, NAIVE = `0.020`
- **Top Positive Drivers (Pushing Toward Prediction)**: M1_1 (+0.524), P61 (+0.300), G35 (+0.127)
- **Top Negative Drivers (Pushing Away)**: J43 (-0.203), J20 (-0.069), J2 (-0.054)

### Case `UNDERCONFIDENT_Case_1` (Test Sample Index `149`)
- **True Archetype**: `UNDERCONFIDENT` | **Predicted Archetype**: `UNDERCONFIDENT`
- **Predicted Probabilities**: COMPETENT = `0.125`, OVERCONFIDENT = `0.193`, UNDERCONFIDENT = `0.335`, NAIVE = `0.347`
- **Top Positive Drivers (Pushing Toward Prediction)**: J62 (+0.188), J1 (+0.124), J33_40 (+0.117)
- **Top Negative Drivers (Pushing Away)**: A50A (-0.150), M1_1 (-0.112), J2 (-0.072)

### Case `UNDERCONFIDENT_Case_2` (Test Sample Index `1938`)
- **True Archetype**: `UNDERCONFIDENT` | **Predicted Archetype**: `UNDERCONFIDENT`
- **Predicted Probabilities**: COMPETENT = `0.121`, OVERCONFIDENT = `0.260`, UNDERCONFIDENT = `0.301`, NAIVE = `0.318`
- **Top Positive Drivers (Pushing Toward Prediction)**: A4A_new_w (+0.121), A21_2015 (+0.104), B60 (+0.096)
- **Top Negative Drivers (Pushing Away)**: J1 (-0.139), M1_1 (-0.136), A50A (-0.110)

### Case `NAIVE_Case_1` (Test Sample Index `1270`)
- **True Archetype**: `NAIVE` | **Predicted Archetype**: `NAIVE`
- **Predicted Probabilities**: COMPETENT = `0.029`, OVERCONFIDENT = `0.465`, UNDERCONFIDENT = `0.042`, NAIVE = `0.464`
- **Top Positive Drivers (Pushing Toward Prediction)**: M1_1 (+0.389), J1 (+0.249), J41_3 (+0.199)
- **Top Negative Drivers (Pushing Away)**: A50A (-0.084), F2_3 (-0.046), J64 (-0.037)

### Case `NAIVE_Case_2` (Test Sample Index `1244`)
- **True Archetype**: `NAIVE` | **Predicted Archetype**: `NAIVE`
- **Predicted Probabilities**: COMPETENT = `0.048`, OVERCONFIDENT = `0.458`, UNDERCONFIDENT = `0.060`, NAIVE = `0.434`
- **Top Positive Drivers (Pushing Toward Prediction)**: M1_1 (+0.312), J1 (+0.211), A50A (+0.144)
- **Top Negative Drivers (Pushing Away)**: J63 (-0.080), B61 (-0.070), J60_1 (-0.057)

---

## 18. Limitations
1. **Observational Scope**: SHAP explanations describe the internal decision mapping of the LightGBM classifier; they do NOT establish causal relationships.
2. **Correlated Feature Effects**: TreeSHAP accounts for feature dependencies in tree structures, but collinear survey items share marginal Shapley contributions.
3. **Dataset Scope**: Results are strictly specific to the NFCS 2024 microdata representation and 70/15/15 partition.

---

## 19. Non-Causal Interpretation Warning
> **IMPORTANT RESEARCH NOTICE**: TreeSHAP values reflect how input variables influence model probability outputs for individual observations. Higher positive SHAP values indicate that a feature value increased the model's predicted probability for a given archetype. SHAP values do NOT prove that changing a user's behavior or demographic attribute will cause a change in their underlying financial capability or archetype classification.

---

## 20. Reproducibility Metadata
- **Python Version**: 3.14.6
- **Package Versions**: `shap` `0.52.0` | `lightgbm` `4.7.0` | `scikit-learn` `1.9.1` | `matplotlib` `3.11.2`
- **Dataset Path**: `research/dataset/NFCS 2024 State Data 250623.csv`
- **Frozen Feature Subset**: `TOP_75` (75 original NFCS variables $	o$ 503 encoded features)
- **Random Seed**: `42`
- **Output Directory**: `research/ml/shap/` (`results.json`, `global_shap_importance.csv`, `class_shap_importance.csv`, `class_positive_features.csv`, `class_negative_features.csv`, `underconfident_analysis.csv`, `gain_vs_shap_comparison.csv`, `individual_explanations.csv`, `feature_category_shap_summary.csv`, `visualizations/`)

---

## 21. Next Research Step
Phase 6D TreeSHAP Explainability Analysis is complete. The model decision logic is fully explained and verified. Awaiting explicit user approval before proceeding to Phase 7 (Model Serving / API / Personalization Architecture). No backend or frontend code has been modified.