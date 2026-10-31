# 6. Phase 6C-R Feature Selection Results

## Feature Reduction & Subset Selection

- **Full Predictor Set**: 121 original candidate variables (673 one-hot encoded features)
- **Selected Predictor Subset**: **75 original variables (`TOP_75`)** (503 one-hot encoded features)
- **Feature Selection Method**: Original-variable $K$-top feature selection evaluated via 5-fold Stratified Cross-Validation on the training partition.
- **5-Fold Training CV Macro F1**: **`0.5195 ± 0.0077`** (Highest CV Macro F1 across all evaluated candidate sub-sets: `TOP_100`=0.5172, `TOP_75`=0.5195, `TOP_50`=0.5167, `TOP_30`=0.5095).

---

## Final Held-Out Test Metrics ($N = 3,755$)

- **Test Accuracy**: **`0.5619`**
- **Macro Precision**: **`0.5189`**
- **Macro Recall**: **`0.5295`**
- **Macro F1**: **`0.5205`**
- **Weighted F1**: **`0.5721`**

---

## Approved Research Wording Guidelines

> **Approved Research Summary Statement**:  
> *"The selected 75-variable subset retained competitive predictive performance while reducing the original predictor set by 38.0%."*  
>  
> *(Note: Do NOT describe removed variables as 'non-informative'.)*
