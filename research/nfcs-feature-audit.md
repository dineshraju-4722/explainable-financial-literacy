# NFCS 2024 Feature Audit & Target Leakage Analysis

## 1. Executive Summary & Audit Scope

To ensure scientific rigor and prevent artificial predictive performance, a comprehensive variable-level audit was conducted across all **133 columns** of the NFCS 2024 State-by-State dataset (`NFCS 2024 State Data 250623.csv`).

### Audit Summary Statistics
- **Total Variables Inspected**: 133
- **Excluded Variables**: 12
  - **Target Component Leakage**: 6 (`M4`, `M6`, `M7`, `M8`, `M9`, `M10`)
  - **Additional Quiz Leakage**: 2 (`M31`, `M50`)
  - **Administrative ID**: 1 (`NFCSID`)
  - **Survey Design Weights**: 3 (`wgt_n2`, `wgt_d2`, `wgt_s3`)
- **Retained Candidate Predictor Features**: **121**

---

## 2. Target Leakage Exclusions (Mandatory Exclusions)

The following 8 variables are strictly excluded from the predictor feature matrix $\mathbf{X}$ because they directly construct or leak information about the target variable:

```
+-----------------------------------------------------------------------------------+
|                            TARGET LEAKAGE EXCLUSIONS                              |
+-----------------------------------------------------------------------------------+
| M4      : Self-assessed financial knowledge (1-7) -> Direct Subjective Component   |
| M6      : Interest rate quiz question             -> Direct Objective Component    |
| M7      : Inflation quiz question                 -> Direct Objective Component    |
| M8      : Bond prices quiz question               -> Direct Objective Component    |
| M9      : Mortgage term quiz question             -> Direct Objective Component    |
| M10     : Stock vs Mutual Fund quiz question      -> Direct Objective Component    |
| M31     : Compound interest doubling time quiz    -> Extended Quiz Leakage         |
| M50     : Disease risk/probability quiz           -> Extended Quiz Leakage         |
+-----------------------------------------------------------------------------------+
```

| Variable | Description | Category | Feature Allowed? | Leakage Risk | Detailed Exclusion Reason |
| :--- | :--- | :--- | :---: | :---: | :--- |
| `M4` | Overall subjective financial knowledge (1-7) | TARGET COMPONENT | **NO** | **HIGH** | Direct input to subjective threshold calculation ($M4 \ge 5$). Including it causes deterministic target leakage. |
| `M6` | Quiz 1: Interest rate calculation ($100 at 2%) | TARGET COMPONENT | **NO** | **HIGH** | Direct input to objective score sum ($S_{	ext{obj}}$). |
| `M7` | Quiz 2: Inflation purchasing power | TARGET COMPONENT | **NO** | **HIGH** | Direct input to objective score sum ($S_{	ext{obj}}$). |
| `M8` | Quiz 3: Bond price & interest rate relationship | TARGET COMPONENT | **NO** | **HIGH** | Direct input to objective score sum ($S_{	ext{obj}}$). |
| `M9` | Quiz 4: 15-yr vs 30-yr mortgage interest | TARGET COMPONENT | **NO** | **HIGH** | Direct input to objective score sum ($S_{	ext{obj}}$). |
| `M10` | Quiz 5: Single stock vs mutual fund risk | TARGET COMPONENT | **NO** | **HIGH** | Direct input to objective score sum ($S_{	ext{obj}}$). |
| `M31` | Quiz: Compound interest loan doubling time | ADDITIONAL QUIZ | **NO** | **HIGH** | Part of NFCS 7-question extended financial literacy quiz. Direct test of literacy skill. |
| `M50` | Quiz: Risk & probability disease indicator | ADDITIONAL QUIZ | **NO** | **HIGH** | Part of NFCS 7-question extended financial literacy quiz. Direct test of numeracy skill. |
| `NFCSID` | Respondent ID Number | Administrative | **NO** | **HIGH** | Arbitrary primary key identifier with no predictive semantic value. |
| `wgt_n2` | National Sample Weight | Weighting | **NO** | **MEDIUM** | Survey sampling weight; not a participant-level behavioral feature. |
| `wgt_d2` | Census Division Sample Weight | Weighting | **NO** | **MEDIUM** | Survey sampling weight; not a participant-level behavioral feature. |
| `wgt_s3` | State Sample Weight | Weighting | **NO** | **MEDIUM** | Survey sampling weight; not a participant-level behavioral feature. |

---

## 3. Inventory of All 133 NFCS Variables & Feature Classification

The complete inventory of all 133 NFCS 2024 variables is documented below:

| Variable | Questionnaire Description / Label | Feature Category | Feature Allowed? | Leakage Risk | Preprocessing Requirement |
| :--- | :--- | :--- | :---: | :---: | :--- |
| `NFCSID` | Respondent ID Number | Administrative / Geographic | **NO** | **HIGH** | Excluded from feature vector |
| `STATEQ` | State FIPS Code (1-51) | Demographics & Geographics | **YES** | **LOW** | Categorical (51 states) |
| `CENSUSDIV` | Census Division (1-9) | Demographics & Geographics | **YES** | **LOW** | Categorical (9 divisions) |
| `CENSUSREG` | Census Region (1-4) | Demographics & Geographics | **YES** | **LOW** | Categorical (4 regions) |
| `A50A` | Gender (Nonbinary randomly assigned) | Demographics & Socioeconomics | **YES** | **LOW** | Binary/Categorical |
| `A3Ar_w` | Age Group (6 categories: 18-24, 25-34, 35-44, 45-54, 55-64, 65+) | Demographics & Socioeconomics | **YES** | **LOW** | Ordinal / Categorical |
| `A50B` | Gender/Age Net combination variable | Demographics & Socioeconomics | **YES** | **LOW** | Categorical |
| `A4A_new_w` | Ethnicity / Race (White, Black, Hispanic, Asian, Other) | Demographics & Socioeconomics | **YES** | **LOW** | Categorical |
| `A5_2015` | Highest Level of Education Completed | Demographics & Socioeconomics | **YES** | **LOW** | Ordinal (1-8) |
| `A6` | Marital Status (Married, Single, Separated/Divorced, Widowed) | Demographics & Socioeconomics | **YES** | **LOW** | Categorical |
| `A7` | Current Living Arrangements (Own house, Rent, Live with parents, etc.) | Demographics & Socioeconomics | **YES** | **LOW** | Categorical |
| `A7A` | Marital Status Net / Summary variable | Demographics & Socioeconomics | **YES** | **LOW** | Categorical |
| `A11` | Number of Financially Dependent Children | Demographics & Socioeconomics | **YES** | **LOW** | Numerical / Ordinal |
| `A8_2021` | Approximate Annual Household Income Range | Demographics & Socioeconomics | **YES** | **LOW** | Ordinal (1-8) |
| `AM21` | U.S. Armed Forces Military Service History | Demographics & Socioeconomics | **YES** | **LOW** | Binary / Categorical (1=Yes, 2=No, 98/99) |
| `X3` | Employment Status (Employed full/part-time, Self-employed, Unemployed, Retired, Homemaker, Student, Disabled) | Demographics & Socioeconomics | **YES** | **LOW** | Categorical |
| `A9` | Spouse/Partner Employment Status | Demographics & Socioeconomics | **YES** | **LOW** | Categorical |
| `A40` | Household Income Drops / Volatility in Past Year | Demographics & Socioeconomics | **YES** | **LOW** | Binary / Categorical |
| `A10` | Primary Household Financial Decision Maker Status | Demographics & Socioeconomics | **YES** | **LOW** | Categorical |
| `A10A` | Spouse/Partner Primary Financial Decision Maker Role | Demographics & Socioeconomics | **YES** | **LOW** | Categorical |
| `A21_2015` | Financial Hardship / Difficulty Covering Expenses in Past Year | Demographics & Socioeconomics | **YES** | **LOW** | Ordinal (1-3) |
| `A14` | Unexpected Financial Shock / Income Decrease Expectation | Demographics & Socioeconomics | **YES** | **LOW** | Categorical |
| `A41` | Health Shock / Medical Expense Financial Impact | Demographics & Socioeconomics | **YES** | **LOW** | Categorical |
| `A61` | Financial Dependence on Family / Friends Support | Demographics & Socioeconomics | **YES** | **LOW** | Categorical |
| `J1` | Overall Satisfaction with Personal Financial Condition (1-10) | Financial Behavior & Stress | **YES** | **LOW** | Ordinal (1-10) |
| `J2` | Spending Relative to Income (Spend less, equal, or more than income) | Financial Behavior & Money Management | **YES** | **LOW** | Ordinal (1-3) |
| `J3` | Difficulty Paying Monthly Bills (Not at all, Somewhat, Very difficult) | Financial Behavior & Money Management | **YES** | **LOW** | Ordinal (1-3) |
| `J4` | Emergency Fund Availability (Set aside 3 months expenses) | Financial Behavior & Money Management | **YES** | **LOW** | Binary (1=Yes, 2=No) |
| `J40` | Capacity to Come Up with $2,000 for Unexpected Emergency in 30 Days | Financial Behavior & Money Management | **YES** | **LOW** | Ordinal (1-4) |
| `J5` | Calculated Retirement Savings Need (Ever tried to figure out savings needed) | Financial Behavior & Money Management | **YES** | **LOW** | Binary (1=Yes, 2=No) |
| `J6` | Established Rainy Day / Emergency Savings Account | Financial Behavior & Money Management | **YES** | **LOW** | Binary (1=Yes, 2=No) |
| `J8` | Checking Account Overdraft Frequency | Financial Behavior & Money Management | **YES** | **LOW** | Ordinal |
| `J9` | Budgeting Practice (Regularly track income and expenses) | Financial Behavior & Money Management | **YES** | **LOW** | Binary / Categorical |
| `J10` | Financial Goal Setting Horizon (Short-term vs Long-term) | Financial Behavior & Money Management | **YES** | **LOW** | Ordinal |
| `J20` | Short-Term Financial Strain / Stress Level | Financial Behavior & Stress | **YES** | **LOW** | Ordinal |
| `J60_1` | Financial Stress Factor: Job / Employment Security | Financial Behavior & Stress | **YES** | **LOW** | Binary indicator |
| `J60_2` | Financial Stress Factor: Health / Medical Costs | Financial Behavior & Stress | **YES** | **LOW** | Binary indicator |
| `J60_3` | Financial Stress Factor: Debt Burden | Financial Behavior & Stress | **YES** | **LOW** | Binary indicator |
| `J60_4` | Financial Stress Factor: Housing Costs / Mortgage | Financial Behavior & Stress | **YES** | **LOW** | Binary indicator |
| `J60_5` | Financial Stress Factor: Retirement Savings Adequacy | Financial Behavior & Stress | **YES** | **LOW** | Binary indicator |
| `J60_6` | Financial Stress Factor: General Inflation / Cost of Living | Financial Behavior & Stress | **YES** | **LOW** | Binary indicator |
| `J60_7` | Financial Stress Factor: Supporting Family / Relatives | Financial Behavior & Stress | **YES** | **LOW** | Binary indicator |
| `J60_90` | Financial Stress Factor: Other Financial Issues | Financial Behavior & Stress | **YES** | **LOW** | Binary indicator |
| `J60_98` | Financial Stress Factor: Don't Know | Financial Behavior & Stress | **YES** | **LOW** | Missing indicator |
| `J60_99` | Financial Stress Factor: Prefer Not to Say | Financial Behavior & Stress | **YES** | **LOW** | Missing indicator |
| `J61` | Overall Financial Anxiety / Stress Rating (1-7) | Financial Behavior & Stress | **YES** | **LOW** | Ordinal (1-7) |
| `J62` | Financial Stress Physical / Mental Health Impact | Financial Behavior & Stress | **YES** | **LOW** | Binary / Ordinal |
| `J32` | Financial Recordkeeping / Account Monitoring Frequency | Financial Behavior & Money Management | **YES** | **LOW** | Ordinal |
| `J33_40` | Use of Digital Mobile Banking / Financial Apps | Financial Behavior & Money Management | **YES** | **LOW** | Ordinal |
| `J63` | Financial Advice Seeking: Professional Financial Advisor | Financial Behavior & Advice | **YES** | **LOW** | Binary |
| `J64` | Financial Advice Seeking: Friends / Family Members | Financial Behavior & Advice | **YES** | **LOW** | Binary |
| `J41_1` | Financial Advice Domain: Investments & Portfolio | Financial Behavior & Advice | **YES** | **LOW** | Binary indicator |
| `J41_2` | Financial Advice Domain: Debt & Credit Management | Financial Behavior & Advice | **YES** | **LOW** | Binary indicator |
| `J41_3` | Financial Advice Domain: Insurance & Risk Management | Financial Behavior & Advice | **YES** | **LOW** | Binary indicator |
| `J42_1` | Source of Financial Information: Internet & Social Media | Financial Behavior & Advice | **YES** | **LOW** | Binary indicator |
| `J42_2` | Source of Financial Information: Books, Courses, Seminars | Financial Behavior & Advice | **YES** | **LOW** | Binary indicator |
| `J43` | Confidence in Finding Reliable Financial Information | Financial Behavior & Advice | **YES** | **LOW** | Ordinal |
| `J65` | Comparison Shopping for Financial Products / Credit Cards | Financial Behavior & Money Management | **YES** | **LOW** | Ordinal |
| `J66` | Automatic Savings / Direct Transfer Practice | Financial Behavior & Money Management | **YES** | **LOW** | Binary |
| `B1` | Ownership of Checking Account | Saving & Banking | **YES** | **LOW** | Binary (1=Yes, 2=No) |
| `B2` | Ownership of Savings Account / Money Market Fund | Saving & Banking | **YES** | **LOW** | Binary (1=Yes, 2=No) |
| `B4` | Ownership of Certificates of Deposit (CDs) | Saving & Banking | **YES** | **LOW** | Binary (1=Yes, 2=No) |
| `B41_1` | Use of Non-Bank Prepaid Debit Cards | Saving & Banking | **YES** | **LOW** | Binary |
| `B41_2` | Use of Mobile P2P Payment Apps (Zelle, Venmo, CashApp) | Saving & Banking | **YES** | **LOW** | Binary |
| `B60` | Unbanked Household Status (No bank account) | Saving & Banking | **YES** | **LOW** | Binary |
| `B31` | Alternative Financial Services (AFS) Use: Check Cashing Service | Saving & Banking | **YES** | **LOW** | Binary |
| `B42` | AFS Use: Short-Term Payday Loan / Pawn Shop Loan | Saving & Banking | **YES** | **LOW** | Binary |
| `B43` | AFS Use: Rent-to-Own Store Services | Saving & Banking | **YES** | **LOW** | Binary |
| `B61` | Overall AFS Usage Summary Index | Saving & Banking | **YES** | **LOW** | Ordinal / Categorical |
| `C1_2012` | Employer-Sponsored Retirement Plan (401k/403b/Pension) Ownership | Investing & Retirement | **YES** | **LOW** | Binary (1=Yes, 2=No) |
| `C2_2012` | Individual Retirement Account (IRA) Ownership | Investing & Retirement | **YES** | **LOW** | Binary (1=Yes, 2=No) |
| `C3_2012` | Taxable Investment Account (Stocks, Bonds, Mutual Funds) Ownership | Investing & Retirement | **YES** | **LOW** | Binary (1=Yes, 2=No) |
| `C4_2012` | Regular Contributions to Retirement Account | Investing & Retirement | **YES** | **LOW** | Binary |
| `C5_2012` | Retirement Account Loan or Hardship Withdrawal History | Investing & Retirement | **YES** | **LOW** | Binary |
| `C10_2012` | Retirement Planning Confidence / Preparedness Rating | Investing & Retirement | **YES** | **LOW** | Ordinal |
| `C11_2012` | Expected Retirement Age / Timeline | Investing & Retirement | **YES** | **LOW** | Categorical |
| `B14A_1` | Ownership of Cryptocurrencies (Bitcoin, Ethereum, etc.) | Investing & Retirement | **YES** | **LOW** | Binary (1=Yes, 2=No) |
| `B14A_60` | Ownership of Fractional Shares / Micro-Investing Apps | Investing & Retirement | **YES** | **LOW** | Binary (1=Yes, 2=No) |
| `EA_1` | Active Trading Frequency in Investment Account | Investing & Retirement | **YES** | **LOW** | Ordinal |
| `E7` | Investment Risk Tolerance Self-Assessment (1-10 Scale) | Investing & Retirement | **YES** | **LOW** | Ordinal (1-10) |
| `E8` | Knowledge of Investment Fees & Expense Ratios | Investing & Retirement | **YES** | **LOW** | Ordinal |
| `E20` | Primary Investment Objective (Safety, Income, Growth, Speculation) | Investing & Retirement | **YES** | **LOW** | Categorical |
| `E15_2015` | Use of Professional Financial Planner / Robo-Advisor for Investments | Investing & Retirement | **YES** | **LOW** | Categorical |
| `E60` | Holding ESG / Socially Responsible Investments | Investing & Retirement | **YES** | **LOW** | Binary |
| `P60` | Ownership of Real Estate Investment / Property Beyond Primary Home | Investing & Retirement | **YES** | **LOW** | Binary |
| `P51` | Ownership of Small Business / Private Equity Stake | Investing & Retirement | **YES** | **LOW** | Binary |
| `P61` | Inherited Assets or Expected Financial Inheritance | Investing & Retirement | **YES** | **LOW** | Binary |
| `F1` | Number of Credit Cards Held (0, 1, 2-3, 4-8, 9+) | Credit & Debt Behavior | **YES** | **LOW** | Ordinal (1-5) |
| `F2_1` | Credit Card Behavior: Paid Full Balance Monthly (Always) | Credit & Debt Behavior | **YES** | **LOW** | Binary |
| `F2_2` | Credit Card Behavior: Carried Balance with Interest Charges | Credit & Debt Behavior | **YES** | **LOW** | Binary |
| `F2_3` | Credit Card Behavior: Paid Minimum Balance Only | Credit & Debt Behavior | **YES** | **LOW** | Binary |
| `F2_4` | Credit Card Behavior: Incurred Late Payment Fees | Credit & Debt Behavior | **YES** | **LOW** | Binary |
| `F2_5` | Credit Card Behavior: Exceeded Credit Limit | Credit & Debt Behavior | **YES** | **LOW** | Binary |
| `F2_6` | Credit Card Behavior: Took Cash Advance on Credit Card | Credit & Debt Behavior | **YES** | **LOW** | Binary |
| `G1` | Auto Loan Ownership Status | Credit & Debt Behavior | **YES** | **LOW** | Binary (1=Yes, 2=No) |
| `G20` | Primary Home Mortgage Ownership Status | Credit & Debt Behavior | **YES** | **LOW** | Binary (1=Yes, 2=No) |
| `G30_1` | Mortgage Distress: Underwater Mortgage (Owe more than value) | Credit & Debt Behavior | **YES** | **LOW** | Binary indicator |
| `G30_2` | Mortgage Distress: Late Mortgage Payment in Past 12 Months | Credit & Debt Behavior | **YES** | **LOW** | Binary indicator |
| `G30_3` | Mortgage Distress: Applied for Mortgage Modification / Relief | Credit & Debt Behavior | **YES** | **LOW** | Binary indicator |
| `G30_4` | Mortgage Distress: Foreclosure Process Initiated | Credit & Debt Behavior | **YES** | **LOW** | Binary indicator |
| `G30_5` | Mortgage Distress: None of the above mortgage problems | Credit & Debt Behavior | **YES** | **LOW** | Binary indicator |
| `G30_97` | Mortgage Distress: Not Applicable | Credit & Debt Behavior | **YES** | **LOW** | Missing indicator |
| `G30_98` | Mortgage Distress: Don't Know | Credit & Debt Behavior | **YES** | **LOW** | Missing indicator |
| `G30_99` | Mortgage Distress: Prefer Not to Say | Credit & Debt Behavior | **YES** | **LOW** | Missing indicator |
| `G35` | Home Equity Line of Credit (HELOC) or Second Mortgage Ownership | Credit & Debt Behavior | **YES** | **LOW** | Binary |
| `G22_2015` | Student Loan Debt Holding Status | Credit & Debt Behavior | **YES** | **LOW** | Binary (1=Yes, 2=No) |
| `G25_1` | Student Loan Distress: Late Payment / Delinquency | Credit & Debt Behavior | **YES** | **LOW** | Binary indicator |
| `G25_2` | Student Loan Distress: Income-Driven Repayment Plan Enrollment | Credit & Debt Behavior | **YES** | **LOW** | Binary indicator |
| `G25_3` | Student Loan Distress: Loan Forgiveness / Discharge Application | Credit & Debt Behavior | **YES** | **LOW** | Binary indicator |
| `G25_4` | Student Loan Distress: Forbearance or Deferment Status | Credit & Debt Behavior | **YES** | **LOW** | Binary indicator |
| `G25_5` | Student Loan Distress: None of the above student loan issues | Credit & Debt Behavior | **YES** | **LOW** | Binary indicator |
| `G60` | Unpaid Medical Debt Holding Status | Credit & Debt Behavior | **YES** | **LOW** | Binary |
| `G38` | Over-Indebtedness Perception (Too much debt right now) | Credit & Debt Behavior | **YES** | **LOW** | Ordinal (1-7) |
| `G23` | Personal / Debt Collection Agency Contact History | Credit & Debt Behavior | **YES** | **LOW** | Binary |
| `H1` | Health Insurance Coverage Status | Insurance & Risk Protection | **YES** | **LOW** | Binary (1=Yes, 2=No) |
| `H30_3` | Life Insurance Policy Ownership | Insurance & Risk Protection | **YES** | **LOW** | Binary (1=Yes, 2=No) |
| `M1_1` | Self-Efficacy in Day-to-Day Financial Management (1-7 Scale) | Financial Self-Assessment | **YES** | **LOW** | Ordinal (1-7) |
| `M4` | Overall Subjective Financial Knowledge Self-Rating (1-7 Scale) | TARGET COMPONENT | **NO** | **HIGH** | EXCLUDED: Target Leakage |
| `M20` | Financial Education Offered in High School, College, or Workplace | Financial Education | **YES** | **LOW** | Categorical |
| `M21_1` | Participated in Financial Education in High School | Financial Education | **YES** | **LOW** | Binary |
| `M21_2_2015` | Participated in Financial Education in College | Financial Education | **YES** | **LOW** | Binary |
| `M21_3` | Participated in Financial Education at Workplace | Financial Education | **YES** | **LOW** | Binary |
| `M21_4` | Participated in Financial Education in Military | Financial Education | **YES** | **LOW** | Binary |
| `M6` | Quiz Question 1: Interest Rate Calculation ($100 at 2%) | TARGET COMPONENT | **NO** | **HIGH** | EXCLUDED: Target Leakage |
| `M7` | Quiz Question 2: Inflation Impact (1% interest vs 2% inflation) | TARGET COMPONENT | **NO** | **HIGH** | EXCLUDED: Target Leakage |
| `M8` | Quiz Question 3: Bond Prices & Interest Rate Inverse Relation | TARGET COMPONENT | **NO** | **HIGH** | EXCLUDED: Target Leakage |
| `M31` | Quiz Question: Compound Interest Loan Doubling Time (Rule of 72) | ADDITIONAL QUIZ QUESTION | **NO** | **HIGH** | EXCLUDED: Literacy Quiz Leakage |
| `M50` | Quiz Question: Risk & Probability Disease Indicator | ADDITIONAL QUIZ QUESTION | **NO** | **HIGH** | EXCLUDED: Literacy Quiz Leakage |
| `M9` | Quiz Question 4: Mortgage Term Length & Total Interest Paid | TARGET COMPONENT | **NO** | **HIGH** | EXCLUDED: Target Leakage |
| `M10` | Quiz Question 5: Stock vs Mutual Fund Risk Diversification | TARGET COMPONENT | **NO** | **HIGH** | EXCLUDED: Target Leakage |
| `wgt_n2` | National Sample Weight (Weight within nation by age/gender, ethnicity, education, division) | Survey Weights | **NO** | **MEDIUM** | EXCLUDED from ML features; used only for population descriptive weighting |
| `wgt_d2` | Census Division Sample Weight (Weight within division by age/gender, ethnicity, education, state) | Survey Weights | **NO** | **MEDIUM** | EXCLUDED from ML features; used only for division descriptive weighting |
| `wgt_s3` | State Sample Weight (Weight within state by age/gender, ethnicity, education) | Survey Weights | **NO** | **MEDIUM** | EXCLUDED from ML features; used only for state descriptive weighting |

---

## 4. Temporal & Predictive Validity Analysis

In predictive modeling using cross-sectional survey microdata, distinguishing between legitimate predictive features and post-outcome information is essential:

1. **Cross-Sectional Timing Limitation**: The NFCS is a cross-sectional survey administered at a single point in time. Predictive models estimate association and classification capability, not longitudinal causality.
2. **Behavioral Proxies as Features**: Financial behaviors (e.g., emergency fund saving `J4`, credit card minimum payments `F2_3`, retirement contributions `C4_2012`) reflect real-world financial decision-making that correlates with financial capability archetypes without leaking the target quiz answers.
3. **Subjective Self-Efficacy**: `M1_1` ("good at dealing with day-to-day financial matters") is a distinct measure of short-term day-to-day transaction self-efficacy, whereas `M4` is global financial knowledge. `M1_1` is retained as a legitimate behavioral self-efficacy predictor.
