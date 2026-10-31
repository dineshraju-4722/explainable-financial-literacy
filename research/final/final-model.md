# 7. Final Model Specification & Confusion Matrix

## Model Architecture & Artifacts

- **Model Classifier**: Class-Weighted LightGBM Classifier (`LightGBM_TOP75_ClassWeighted`)
- **Version**: `1.0.0`
- **Original NFCS Predictors**: `75`
- **Encoded Features**: `503` binary features (One-Hot Encoded)
- **Class Weighting**: Balanced Inverse-Frequency (`COMPETENT`: 0.5713, `OVERCONFIDENT`: 1.0948, `NAIVE`: 1.3586, `UNDERCONFIDENT`: 1.6661)

---

## Final Selected Hyperparameters

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

## Publication Confusion Matrix (Held-Out Test Set, $N = 3,755$)

Order of rows (True Class) and columns (Predicted Class): `[COMPETENT, OVERCONFIDENT, UNDERCONFIDENT, NAIVE]`.

| True Class \ Predicted | COMPETENT | OVERCONFIDENT | UNDERCONFIDENT | NAIVE | Total | Class Precision | Class Recall | Class F1 |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **COMPETENT** | **1,064** | 271 | 221 | 87 | 1,643 | 0.7806 | 0.6476 | **0.7079** |
| **OVERCONFIDENT** | 162 | **423** | 94 | 179 | 858 | 0.4890 | 0.4930 | **0.4910** |
| **UNDERCONFIDENT** | 114 | 64 | **231** | 154 | 563 | 0.3231 | 0.4103 | **0.3615** |
| **NAIVE** | 23 | 107 | 169 | **392** | 691 | 0.4828 | 0.5673 | **0.5216** |
| **Total Predicted** | 1,363 | 865 | 715 | 812 | 3,755 | **0.5189** | **0.5295** | **0.5205** |
