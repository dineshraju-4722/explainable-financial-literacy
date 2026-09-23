# Feature Engineering & ML Vector Specification

## Overview

The `FeatureEngineeringService` converts completed financial literacy assessment data into a stable, deterministic numerical feature vector (`AssessmentFeatureVector`). This vector serves as the primary standardized input representation for future Machine Learning models (such as LightGBM, XGBoost, and Random Forest in Phase 5+).

---

## Feature Vector Structure

The feature vector contains overall performance metrics, completion metadata, and comprehensive maps for category and difficulty feature breakdown.

```json
{
  "overallPercentage": 80.0,
  "totalQuestions": 10,
  "answeredQuestions": 10,
  "correctAnswers": 8,
  "incorrectAnswers": 2,
  "proficiencyLevel": "ADVANCED",
  "assessmentType": "INITIAL",
  "completionStatus": "COMPLETED",
  "categoryFeatures": {
    "BUDGETING": { "questionCount": 3, "correctCount": 3, "percentage": 100.0 },
    "SAVING": { "questionCount": 2, "correctCount": 1, "percentage": 50.0 },
    "CREDIT": { "questionCount": 0, "correctCount": 0, "percentage": null }
  },
  "difficultyFeatures": {
    "BEGINNER": { "questionCount": 4, "correctCount": 4, "percentage": 100.0 },
    "INTERMEDIATE": { "questionCount": 4, "correctCount": 3, "percentage": 75.0 },
    "ADVANCED": { "questionCount": 2, "correctCount": 1, "percentage": 50.0 }
  }
}
```

---

## Missing-Value Handling (`null` vs `0.0%`)

To ensure clean machine learning model interpretability, missing categories and difficulty levels are explicitly distinguished from poor performance:

* **`percentage = 0.0%`**: Questions **were present and attempted** in this category/difficulty, but 0 questions were answered correctly.
* **`percentage = null`**: **No questions were included** or attempted for this category/difficulty in the assessment.

> [!IMPORTANT]
> Models MUST NOT impute missing categories as 0.0%, as doing so would misrepresent unassessed domain topics as failed domain topics.

---

## Feature Taxonomy

| Feature Name | Type | Range / Domain | Description |
| :--- | :--- | :--- | :--- |
| `overallPercentage` | `Double` | `[0.0, 100.0]` | Aggregated score percentage |
| `totalQuestions` | `Integer` | $\ge 0$ | Number of total questions in assessment |
| `answeredQuestions` | `Integer` | $\ge 0$ | Number of questions answered |
| `correctAnswers` | `Integer` | $\ge 0$ | Total correct answers |
| `incorrectAnswers` | `Integer` | $\ge 0$ | Total incorrect answers |
| `proficiencyLevel` | `String` | `BEGINNER`, `INTERMEDIATE`, `ADVANCED` | Authoritative baseline proficiency level |
| `assessmentType` | `Enum` | `INITIAL`, `REASSESSMENT` | Assessment context |
| `completionStatus` | `Enum` | `COMPLETED` | Assessment status snapshot |
| `categoryFeatures` | `Map` | All 11 `FinancialLiteracyCategory` keys | Per-category question count, correct count, & percentage (`null` if unattempted) |
| `difficultyFeatures` | `Map` | All 3 `DifficultyLevel` keys | Per-difficulty question count, correct count, & percentage (`null` if unattempted) |

---

## Determinism & Rounding

* Feature extraction is completely stateless and deterministic.
* All percentage metrics are rounded to 2 decimal places using `RoundingMode.HALF_UP`.
