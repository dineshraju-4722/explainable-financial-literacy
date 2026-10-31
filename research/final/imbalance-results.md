# 4. Phase 6A Class Imbalance Experiment Results

## Class Imbalance Strategy Comparison (Held-Out Test Set, $N = 3,755$)

| Strategy | Test Acc | Test Weighted F1 | **Test Macro F1** | **COMPETENT F1** | **OVERCONFIDENT F1** | **NAIVE F1** | **UNDERCONFIDENT F1** | 5-Fold CV Macro F1 |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **`LGBM_BASELINE`** | 0.5901 | 0.5656 | **0.4972** | 0.7411 | 0.4551 | 0.5437 | **0.2491** | 0.4838 $\pm$ 0.0089 |
| **`LGBM_CLASS_WEIGHTED`** | 0.5587 | 0.5692 | **0.5182** | 0.7064 | 0.4722 | 0.5333 | **0.3608** | 0.5112 $\pm$ 0.0084 |
| **`LGBM_RANDOM_OVERSAMPLING`** | 0.5553 | 0.5659 | **0.5141** | 0.7053 | 0.4677 | 0.5274 | **0.3559** | 0.5079 $\pm$ 0.0101 |
| **`LGBM_SMOTE`** | 0.5819 | 0.5635 | **0.5004** | 0.7326 | 0.4403 | 0.5308 | **0.2977** | 0.5000 $\pm$ 0.0052 |

*Note: SMOTE-NC was evaluated as NOT APPLICABLE because 100% of the candidate features in the NFCS dataset are nominal categorical variables.*

---

## Imbalance Method Selection Rationale

- **Macro F1 Winner**: Inverse-frequency class weighting (`LGBM_CLASS_WEIGHTED`) achieved the highest Test Macro F1 (**`0.5182`**) and 5-Fold CV Macro F1 (**`0.5112`**).
- **Minority Archetype Recovery**: Class weighting boosted `UNDERCONFIDENT` F1 from `0.2491` to **`0.3608`** (+44.8% relative gain) and `UNDERCONFIDENT` recall from 18.12% to **51.86%**.
