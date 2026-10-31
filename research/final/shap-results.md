# 8. Phase 6D TreeSHAP Explainability Results

## TreeSHAP Methodology

- **Algorithm**: `shap.TreeExplainer` applied to the locked Phase 6C-R `TOP_75` LightGBM model.
- **Evaluation Partition**: Held-out test set ($N_{\text{test}} = 3,755$).
- **Aggregation**: Shapley contributions computed across 503 binary features and aggregated back to the 75 original NFCS predictor variables by summing signed contributions per observation and averaging absolute values globally:
  $$\text{Mean Abs SHAP}(V) = \frac{1}{4 N} \sum_{c=1}^{4} \sum_{i=1}^{N} |\text{SHAP}_{i, V, c}|$$

---

## Top 20 Global Feature Importance (Mean Absolute SHAP)

| Rank | Variable | Official Description | Phase 4A Taxonomy Category | Global Mean Abs SHAP | Normalized Share % |
| :---: | :--- | :--- | :--- | :---: | :---: |
| 1 | **M1_1** | Overall Financial Knowledge (Self-Assessment Scale 1-7) | Financial Education & Self-Assessment | **0.2803** | **11.50%** |
| 2 | **A50A** | Gender Identity (Female / Male) | Demographics & Socioeconomics | **0.1202** | **4.93%** |
| 3 | **J1** | Difficulty Covering Monthly Expenses | Financial Behavior & Money Management | **0.1039** | **4.26%** |
| 4 | **J8** | Checking Account Overdraft Frequency | Financial Behavior & Money Management | **0.0753** | **3.09%** |
| 5 | **A14** | Household Income Volatility / Unexpected Drop | Demographics & Socioeconomics | **0.0703** | **2.88%** |
| 6 | **B14A_1** | Cryptocurrency Ownership Status | Investing & Retirement | **0.0646** | **2.65%** |
| 7 | **J43** | Confidence in Coming Up with $2,000 in Emergency | Financial Behavior & Money Management | **0.0645** | **2.65%** |
| 8 | **J20** | Overall Financial Satisfaction (Scale 1-10) | Financial Behavior & Money Management | **0.0644** | **2.64%** |
| 9 | **A5_2015** | Highest Education Level Completed | Demographics & Socioeconomics | **0.0629** | **2.58%** |
| 10 | **B60** | Unbanked Status / No Checking or Savings Account | Saving & Banking | **0.0617** | **2.53%** |
| 11 | **J2** | Spending Behavior Relative to Income | Financial Behavior & Money Management | **0.0580** | **2.38%** |
| 12 | **J32** | Financial Stress / Anxiety in Life | Financial Behavior & Money Management | **0.0553** | **2.19%** |
| 13 | **A21_2015** | Felt Financial Hardship in Past Year | Demographics & Socioeconomics | **0.0409** | **1.68%** |
| 14 | **A4A_new_w** | Race / Ethnicity | Demographics & Socioeconomics | **0.0400** | **1.64%** |
| 15 | **F2_3** | Paid Minimum Only on Credit Card | Credit & Debt Behavior | **0.0389** | **1.59%** |
| 16 | **A50B** | Ethnicity / Racial Identity | Demographics & Socioeconomics | **0.0385** | **1.58%** |
| 17 | **J33_40** | Short-term Financial Emergency Preparedness | Financial Behavior & Money Management | **0.0370** | **1.52%** |
| 18 | **J60_1** | Stress Factor: Job / Employment Security | Financial Behavior & Money Management | **0.0357** | **1.47%** |
| 19 | **M21_2_2015** | Participated in Financial Education in College | Financial Education & Self-Assessment | **0.0356** | **1.46%** |
| 20 | **A3Ar_w** | Age Category Bracket | Demographics & Socioeconomics | **0.0354** | **1.45%** |

---

## Category-Level SHAP Distribution

- **Financial Behavior & Money Management**: **29.10%** (21 variables)
- **Demographics & Socioeconomics**: **22.51%** (15 variables)
- **Financial Education & Self-Assessment**: **14.16%** (3 variables)
- **Credit & Debt Behavior**: **14.08%** (17 variables)
- **Investing & Retirement**: **10.58%** (9 variables)
- **Saving & Banking**: **7.73%** (7 variables)
- **Demographics & Geographics**: **1.23%** (2 variables)
- **Insurance & Risk Protection**: **0.60%** (1 variable)

---

## UNDERCONFIDENT Deep-Dive Summary

For the vulnerable `UNDERCONFIDENT` archetype ($S_{\text{obj}} \ge 3, M4 \le 4$), top positive drivers pushing model predictions toward the class include unbanked status (`B60`, +0.1742), low day-to-day financial self-efficacy (`M1_1`, +0.1539), emergency fund availability (`J6`, +0.1447), monthly expense difficulty (`J1`, +0.1328), and gender identity (`A50A`, +0.1157).

---

## Causal Interpretation Disclaimer

> **MANDATORY RESEARCH DISCLAIMER**:  
> *TreeSHAP values measure post-hoc marginal feature contributions to individual model probability predictions. SHAP values explain model decision logic; they do NOT establish causal mechanisms or guarantee that altering a user's behavior will cause a change in underlying financial capability.*
