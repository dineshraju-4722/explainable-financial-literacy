# 10. Publication & Presentation Claims Guidelines

## 1. Safe Academic Claims

The following claims are fully supported by empirical artifacts and may be used in papers and presentations:

- *"The platform integrates a multi-class machine learning classifier with TreeSHAP explainability and a deterministic educational recommendation engine."*
- *"Trained and evaluated on $25,030$ valid survey observations from the FINRA National Financial Capability Study (NFCS) 2024."*
- *"Class-weighted LightGBM achieved 56.19% accuracy and 0.5205 macro F1 across four financial capability archetypes."*
- *"Feature selection using 5-fold cross-validation reduced the original predictor set by 38.0% (from 121 to 75 variables) while preserving predictive performance."*
- *"TreeSHAP feature attributions identify key domain predictors shifting archetype probability distributions for individual observations."*

---

## 2. Claims to Avoid & Mandatory Qualifications

The following terminology and claims are **STRICTLY PROHIBITED** or require explicit qualification:

| Prohibited Term / Claim | Reason for Restriction | Approved Qualified Alternative |
| :--- | :--- | :--- |
| **"Causes" / "Causal link"** | NFCS dataset is cross-sectional; SHAP measures model behavior. | Use *"associated with"*, *"predictive of"*, or *"contributed to model output"*. |
| **"Improves financial literacy"** | No longitudinal RCT intervention study conducted. | Use *"designed to deliver targeted educational content for identified knowledge gaps."* |
| **"Improves financial outcomes"** | No real-world financial tracking conducted in scope. | Use *"provides curated educational modules tailored to weak domain categories."* |
| **"Best model"** | LightGBM won on accuracy/weighted F1, but Logistic Regression had slightly higher baseline macro F1. | Use *"LightGBM achieved the highest accuracy and weighted F1 among evaluated baseline models."* |
| **"Non-informative variables"** | Feature selection retained top 75 variables based on CV macro F1. | Use *"retained competitive predictive performance while reducing the original predictor set."* |
