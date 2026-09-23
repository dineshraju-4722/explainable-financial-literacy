# Phase 3 Assessment Scoring Methodology

## 1. Context & Purpose

This document specifies the **Phase 3 Baseline Deterministic Scoring Model** for the **Financial Literacy Platform**.

> **Important Research Scope Note**: The Phase 3 scoring engine is a deterministic baseline calculation. It is **NOT** an ML-based prediction model. Machine learning classifications (Decision Tree, Random Forest, LightGBM, SVM) and SHAP feature explanations will be introduced in subsequent phases.

---

## 2. Scoring Formula

Each objective assessment question option contains a backend-only boolean flag `isCorrect`.

### Question Score Allocation
- **Correct Answer Selected**: $1.0$ point
- **Incorrect Answer Selected**: $0.0$ points

### Metrics Calculation
$$\text{Total Score} = \sum_{i=1}^{N} \text{Score}(Q_i)$$

$$\text{Percentage} = \left( \frac{\text{Total Score}}{N} \right) \times 100$$

Where $N$ represents the total number of answered questions within the assessment attempt.

---

## 3. Baseline Proficiency Level Classification

Based on objective percentage score, assessments are assigned a deterministic baseline level:

| Percentage Range | Proficiency Level | Baseline Interpretation |
| :--- | :--- | :--- |
| **0.0% – 39.9%** | `BEGINNER` | Low objective financial knowledge; requires fundamental education across key domains. |
| **40.0% – 69.9%** | `INTERMEDIATE` | Moderate knowledge; specific domain gaps identified. |
| **70.0% – 100.0%** | `ADVANCED` | High objective financial knowledge. |

*Note: These baseline thresholds are configurable and serve as reference benchmarks during initial data collection.*
