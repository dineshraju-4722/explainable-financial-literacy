# 3. Phase 5 Baseline Model Comparison

## Unweighted Baseline Performance (Held-Out Test Set, $N = 3,755$)

All baseline models were trained on 17,521 training samples (121 raw candidate predictors, 673 one-hot encoded features) without synthetic oversampling or class weighting:

| Model Algorithm | Test Accuracy | Test Macro Precision | Test Macro Recall | **Test Macro F1** | **Test Weighted F1** | 5-Fold Train CV Macro F1 |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| **Multinomial Logistic Regression** | 0.5798 | 0.5134 | 0.4977 | **0.4991** | **0.5637** | 0.4937 $\pm$ 0.0039 |
| **Random Forest** | 0.5609 | 0.4833 | 0.4480 | **0.4281** | **0.5118** | 0.4235 $\pm$ 0.0054 |
| **LightGBM** | 0.5901 | 0.5245 | 0.5014 | **0.4972** | **0.5656** | 0.4838 $\pm$ 0.0089 |

---

## Model Evaluation Wording

> **Approved Research Summary Statement**:  
> *"LightGBM achieved the highest accuracy and weighted F1 among the three baseline models, while logistic regression achieved a slightly higher macro F1."*
