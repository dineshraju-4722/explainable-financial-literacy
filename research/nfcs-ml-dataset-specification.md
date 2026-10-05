# NFCS 2024 ML Dataset Specification & Preprocessing Pipeline

## 1. Dataset Overview & Source Version

This document defines the formal dataset specification and preprocessing requirements for the machine learning pipeline in Phase 5.

- **Primary Source File**: `NFCS 2024 State Data 250623.csv`
- **Data Publisher**: FINRA Investor Education Foundation (2024 National Financial Capability Study)
- **Total Raw Observation Count**: 25,539 rows
- **Valid Model Observation Count**: **25,030 rows** (after non-substantive target filtering)
- **Total Raw Columns**: 133
- **Predictor Feature Matrix Dimension**: $25,030 	imes 121$

---

## 2. Target Variable Encoding Specification

- **Target Column Name**: `financial_literacy_class`
- **Logical Data Type**: Categorical Enum / String
- **Target Value Encoding**:

| Class Name | Integer Code | One-Hot Representation | Sample Count | Sample Percentage |
| :--- | :---: | :---: | :---: | :---: |
| **COMPETENT** | `0` | `[1, 0, 0, 0]` | 10,953 | 43.76% |
| **OVERCONFIDENT** | `1` | `[0, 1, 0, 0]` | 5,716 | 22.84% |
| **NAIVE** | `2` | `[0, 0, 1, 0]` | 4,605 | 18.40% |
| **UNDERCONFIDENT** | `3` | `[0, 0, 0, 1]` | 3,756 | 15.01% |

---

## 3. Feature Structure & Taxonomy

The 121 candidate features are organized into 8 thematic domain categories:

```
                            121 PREDICTOR FEATURES
                                      │
 ┌───────────┬───────────┬────────────┼────────────┬───────────┬───────────┬───────────┐
 ▼           ▼           ▼            ▼            ▼           ▼           ▼           ▼
Demo (20)   Money (19)  Save (9)    Retire (18)  Debt (21)   Insure (2)  Edu (5)     Geog (3)
```

1. **Demographics & Socioeconomics** (20 features): Age (`A3Ar_w`), Gender (`A50A`), Ethnicity (`A4A_new_w`), Education (`A5_2015`), Income (`A8_2021`), Marital Status (`A6`, `A7A`), Children (`A11`), Employment (`X3`, `A9`), Income Shock (`A40`, `A14`, `A41`), Financial Hardship (`A21_2015`), Family Dependence (`A61`), Military (`AM21`), Financial Decision Role (`A10`, `A10A`).
2. **Financial Behavior & Money Management** (19 features): Satisfaction (`J1`), Spending vs Income (`J2`), Bill Difficulty (`J3`), Emergency Fund (`J4`), $2k Emergency Capacity (`J40`), Rainy Day Account (`J6`), Overdraft (`J8`), Budgeting (`J9`), Goal Horizon (`J10`), Stress Factors (`J60_1`..`J60_90`, `J61`, `J62`), Advice Seeking (`J63`, `J64`), Comparison Shopping (`J65`), Auto-Savings (`J66`).
3. **Saving & Banking** (9 features): Checking (`B1`), Savings (`B2`), CDs (`B4`), Prepaid Cards (`B41_1`), Mobile P2P (`B41_2`), Unbanked Status (`B60`), AFS Check Cashing (`B31`), AFS Payday (`B42`), AFS Rent-to-Own (`B43`).
4. **Investing & Retirement** (18 features): 401k/Pension (`C1_2012`), IRA (`C2_2012`), Taxable Stocks (`C3_2012`), Regular Contributions (`C4_2012`), Hardship Withdrawal (`C5_2012`), Crypto (`B14A_1`), Fractional Shares (`B14A_60`), Risk Tolerance (`E7`), Trading Frequency (`EA_1`), ESG (`E60`), Real Estate (`P60`), Business Ownership (`P51`), Inheritance (`P61`).
5. **Credit & Debt Behavior** (21 features): Credit Cards (`F1`), Paid Full (`F2_1`), Carried Balance (`F2_2`), Minimum Payment (`F2_3`), Late Fee (`F2_4`), Over Limit (`F2_5`), Cash Advance (`F2_6`), Auto Loan (`G1`), Mortgage (`G20`), Mortgage Distress (`G30_1`..`G30_5`), HELOC (`G35`), Student Loan (`G22_2015`), Student Loan Distress (`G25_1`..`G25_5`), Medical Debt (`G60`), Over-Indebtedness (`G38`), Collections (`G23`).
6. **Insurance & Risk Protection** (2 features): Health Insurance (`H1`), Life Insurance (`H30_3`).
7. **Financial Education & Self-Assessment** (5 features): Self-Efficacy (`M1_1`), Education Offered (`M20`), High School Edu (`M21_1`), College Edu (`M21_2_2015`), Workplace Edu (`M21_3`).
8. **Demographics & Geographics** (3 features): State (`STATEQ`), Division (`CENSUSDIV`), Region (`CENSUSREG`).

---

## 4. Missing Value & Non-Substantive Code Policy

NFCS uses standardized non-substantive response codes:
- `98`: "Don't know"
- `99`: "Prefer not to say" / "Refused"

### Standardized Preprocessing Rules

```
+-----------------------------------------------------------------------------------+
|                           MISSING VALUE IMPUTATION POLICY                         |
+-----------------------------------------------------------------------------------+
| Categorical Features  : Treat 98 and 99 as explicit categorical levels            |
|                         ("DON_T_KNOW", "PREFER_NOT_TO_SAY").                      |
| Ordinal Features      : Encode 98/99 as neutral midpoint or missing indicator.    |
| Binary Indicators     : 1 = Yes, 0 = No (98/99 mapped to 0 or explicit indicator) |
| Continuous Features   : Use median imputation with missing indicator flags.       |
| Gradient Boosted Trees: Native handling of NaN/missing splits (LightGBM/XGBoost).|
+-----------------------------------------------------------------------------------+
```

1. **Categorical Predictors**: Preserve `98` and `99` as distinct valid categories to capture implicit uncertainty or non-response bias.
2. **Binary Predictors**: Map `1` (Yes) $	o 1$, `2` (No) $	o 0$, and `98/99` $	o 0$ with an auxiliary binary missing flag.
3. **Ordinal Scale Predictors**: Scale values $1-7$ preserved; `98/99` imputed to neutral value or handled natively by tree-based algorithms (LightGBM / XGBoost handles NaN values natively during split finding).

---

## 5. Survey Weighting Strategy & Evaluation Rules

The NFCS dataset contains three survey weighting variables:
- `wgt_n2`: National weight
- `wgt_d2`: Divisional weight
- `wgt_s3`: State weight

### Weighting Decisions & Justification

| Application Domain | Weighting Recommendation | Scientific Rationale |
| :--- | :--- | :--- |
| **Descriptive Statistics** | **Apply `wgt_s3` / `wgt_n2`** | Ensures population-representative demographic percentages. |
| **Model Training** | **Unweighted Base / Weighted Experiment** | *REQUIRES RESEARCH JUSTIFICATION*. Training tree models with sample weights can distort loss landscapes if weights reflect sampling strategy rather than noise variance. Baseline training will evaluate both unweighted and weighted loss functions. |
| **Model Evaluation** | **Unweighted & Weighted Metrics** | Report both raw sample accuracy/F1-score and weighted population F1-score to evaluate population generalization. |

---

## 6. Train / Validation / Test Partitioning Strategy

To prevent data leakage during hyperparameter tuning and model assessment, the dataset will be split using **Stratified K-Fold Cross-Validation**:

- **Splitting Method**: Stratified 5-Fold Cross-Validation ($80\%$ Train/Val, $20\%$ Held-out Test)
- **Train/Test Holdout**: $80\%$ Development Set ($20,024$ rows), $20\%$ Test Set ($5,006$ rows)
- **Stratification Variable**: `financial_literacy_class` (ensures exact $43.76\% / 22.84\% / 18.40\% / 15.01\%$ class proportions across all folds)
- **Random Seed**: Fixed global seed `RANDOM_STATE = 42` for exact reproducibility.

---

## 7. Compliance Checklist for Phase 4A

- [x] Verified actual NFCS variable names against raw CSV and PDF codebook.
- [x] Recalculated target variable directly from raw dataset ($25,030$ valid records verified).
- [x] Verified exact 4-class target distribution.
- [x] Excluded `M4`, `M6`, `M7`, `M8`, `M9`, `M10` and extended quiz items `M31`, `M50` from candidate predictors.
- [x] Audited all 133 variables without introducing mock or synthetic data.
- [x] Deferred ML model training, LightGBM implementation, and SHAP calculation to Phase 5B.
- [x] Preserved existing Spring Boot backend APIs and domain models intact.
