# Research Methodology Specification

## 1. Reference Foundation

This project extends the methodology published by **Alex Yue Feng Zhu (2025)** (*Technology in Society*, DOI: [10.1016/j.techsoc.2024.102797](https://doi.org/10.1016/j.techsoc.2024.102797)).

### Core Definitions

- **Definition 1 (Binary Objective Knowledge)**: Low vs. High objective financial knowledge score.
- **Definition 2 (Subjective vs. Objective Mismatch)**:
  - *Financially Competent*: High objective knowledge, high subjective confidence.
  - *Overconfident*: Low objective knowledge, high subjective confidence.
  - *Underconfident*: High objective knowledge, low subjective confidence.
  - *Naïve / Incompetent*: Low objective knowledge, low subjective confidence.

---

## 2. Research Extensions

### Extension A: Granular Weak-Area Identification
Instead of evaluating overall literacy as a monolith, our system evaluates performance across 10 specific financial domains:
1. Saving
2. Budgeting
3. Interest & Simple Interest
4. Compound Interest
5. Inflation
6. Loans & Debt Management
7. Investment & Portfolio Basics
8. Financial Risk Management
9. Insurance & Asset Protection
10. Financial Decision-Making

### Extension B: Closed-Loop Personalized Education
Connecting diagnostic outcome directly to targeted learning modules, followed by post-learning reassessment to calculate percentage point growth.

### Extension C: Human-Centric Explainable AI (SHAP)
Using SHAP values translated into non-technical natural language summaries to highlight the behavior and background features driving a user's literacy classification.

---

## 3. Data Integrity & Anti-Leakage Rules

To maintain experimental validity:
1. **No Target Leakage**: The target label (derived from objective knowledge score) must **never** be used as an input feature in the ML model. Input features consist strictly of independent financial behaviors, financial confidence, attitudes, and educational background.
2. **Stratified 5-Fold Cross Validation**: Model selection and evaluation must use 5-fold cross-validation to account for target distribution balance.
3. **No Synthetic Results**: All model evaluations and confusion matrices must be generated from empirical dataset runs.
