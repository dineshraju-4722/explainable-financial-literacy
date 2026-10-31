# 5. Phase 6B Hyperparameter Optimization Results

## Experimental Setup

- **Search Algorithm**: `RandomizedSearchCV` (`n_iter = 30`)
- **Cross-Validation**: 5-Fold Stratified K-Fold on Training Set ($N_{\text{train}} = 17,521$)
- **Random Seed**: `42`
- **Optimization Metric**: `f1_macro`

---

## Optimal Selected Hyperparameters

```json
{
  "n_estimators": 250,
  "learning_rate": 0.05,
  "num_leaves": 15,
  "max_depth": 8,
  "min_child_samples": 30,
  "subsample": 0.8,
  "subsample_freq": 5,
  "colsample_bytree": 0.7,
  "reg_alpha": 0.1,
  "reg_lambda": 0.0,
  "min_split_gain": 0.01
}
```

---

## Performance Progression

- **5-Fold Cross-Validation Macro F1**: **`0.5181 ± 0.0090`**
- **Held-Out Test Set Metrics ($N = 3,755$, Evaluated ONCE post-tuning)**:
  - **Accuracy**: **`0.5640`**
  - **Macro Precision**: **`0.5213`**
  - **Macro Recall**: **`0.5339`**
  - **Macro F1**: **`0.5237`**
  - **Weighted F1**: **`0.5735`**
