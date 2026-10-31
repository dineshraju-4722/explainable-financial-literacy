# 1. Dataset Summary & Processing Pipeline

## Microdata Overview

- **Source Dataset**: FINRA Investor Education Foundation **National Financial Capability Study (NFCS) 2024 State-by-State Survey**
- **Raw Observations ($N_{\text{raw}}$)**: **25,539**
- **Total Variables**: **133**
- **Non-Substantive `M4` Exclusions**: **509** (`98` = 403, `99` = 106)
- **Valid Observations ($N_{\text{valid}}$)**: **25,030**
- **Raw Candidate Predictors**: **121**
- **Final Selected Predictors (`TOP_75`)**: **75** (503 encoded binary features)

---

## Processing Pipeline Summary

| Processing Stage | Input Records | Exclusions / Reduction | Output Records / Features | Justification / Note |
| :--- | :---: | :---: | :---: | :--- |
| **Raw Survey Microdata** | 25,539 | 0 | 25,539 | FINRA NFCS 2024 State Data |
| **Target Filtering** | 25,539 | 509 | 25,030 | Excluded non-substantive `M4` ratings (`98` Don't know, `99` Prefer not to say) |
| **Target Construction** | 25,030 | 0 | 25,030 | Created 4-class archetype label combining objective quiz & subjective rating |
| **Leakage Exclusion** | 133 vars | 12 vars | 121 vars | Excluded 6 target components (`M4`, `M6`–`M10`), 2 quiz items (`M31`, `M50`), `NFCSID`, & 3 weights |
| **One-Hot Encoding** | 121 vars | 0 | 673 encoded cols | Transformed nominal discrete items into binary indicators preserving missing codes |
| **Feature Selection (`TOP_75`)** | 121 vars | 46 vars | 75 vars (503 encoded cols) | Selected top original variables based on 5-fold CV Macro F1 on training set |
