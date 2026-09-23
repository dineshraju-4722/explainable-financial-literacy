# Assessment Analytics Specification

## Overview

Phase 4 introduces an explainable assessment analytics layer designed to decompose an assessment's baseline score into category-level, difficulty-level, and question-level insights without modifying the Phase 3 authoritative deterministic score engine.

---

## Key Principles

1. **Deterministic Preservation**: The baseline score computed by `AssessmentScoringService` (`AssessmentResult`) remains the authoritative score for Phase 3/4. Phase 4 provides analytical metrics derived from raw responses and question metadata.
2. **State Scoping**: Analytics calculations are valid **only** for assessments in `COMPLETED` status. Requests for `IN_PROGRESS` or `ABANDONED` assessments are rejected with HTTP 409 CONFLICT.
3. **Security Boundary**: Participant-facing analytics endpoints return aggregated summary statistics (totals, correct/incorrect counts, category percentages) and **never** expose question answer keys or option correctness flags.

---

## Analytical Metrics Formulae

### 1. Overall Metrics
* **Total Questions Attempted ($N$)**: Total count of responses stored for the assessment.
* **Correct Answers ($C$)**: Number of responses where selected option's `isCorrect == true`.
* **Incorrect Answers ($I$)**: $I = N - C$.
* **Overall Percentage ($P_{overall}$)**:
  $$P_{overall} = \text{round}_{2}\left(\frac{C}{N} \times 100.0\right)$$
* **Proficiency Level**:
  * $0.0\% - 39.9\%$: `BEGINNER`
  * $40.0\% - 69.9\%$: `INTERMEDIATE`
  * $70.0\% - 100.0\%$: `ADVANCED`

### 2. Category Analytics
Calculated for every `FinancialLiteracyCategory` represented in the completed assessment:
* **Category Total Questions ($N_{cat}$)**: Count of questions attempted in category `cat`.
* **Category Correct Answers ($C_{cat}$)**: Count of correct responses in category `cat`.
* **Category Incorrect Answers ($I_{cat}$)**: $I_{cat} = N_{cat} - C_{cat}$.
* **Category Score**: $S_{cat} = C_{cat}$.
* **Category Percentage ($P_{cat}$)**:
  $$P_{cat} = \text{round}_{2}\left(\frac{C_{cat}}{N_{cat}} \times 100.0\right)$$

### 3. Difficulty Analytics
Calculated for every `DifficultyLevel` (`BEGINNER`, `INTERMEDIATE`, `ADVANCED`) represented in the completed assessment:
* **Difficulty Total Questions ($N_{diff}$)**: Count of questions attempted at difficulty level `diff`.
* **Difficulty Correct Answers ($C_{diff}$)**: Count of correct responses at difficulty `diff`.
* **Difficulty Incorrect Answers ($I_{diff}$)**: $I_{diff} = N_{diff} - C_{diff}$.
* **Difficulty Percentage ($P_{diff}$)**:
  $$P_{diff} = \text{round}_{2}\left(\frac{C_{diff}}{N_{diff}} \times 100.0\right)$$

---

## State & Exception Handling

| Condition | Action / Exception | HTTP Status |
| :--- | :--- | :--- |
| Assessment ID not found | `ResourceNotFoundException` | `404 NOT FOUND` |
| Assessment status is `IN_PROGRESS` or `ABANDONED` | `IllegalStateTransitionException` | `409 CONFLICT` |
| Assessment result missing for completed assessment | `ResourceNotFoundException` | `404 NOT FOUND` |

---

## Rounding Policy

All percentages use half-up rounding to two decimal places:
$$\text{round}_{2}(x) = \text{BigDecimal.valueOf}(x).\text{setScale}(2, \text{RoundingMode.HALF_UP}).\text{doubleValue}()$$
This guarantees consistent numerical representation across overall, category, and difficulty metrics.
