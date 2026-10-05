# Personalized Financial Education Engine Specification

## 1. Purpose and Overview

The **Personalized Financial Education Engine** is a deterministic, rule-based recommendation system built on top of the FINRA NFCS Financial Literacy Archetype machine learning system. 

It implements Phase 7D of the research project:
*"Explainable Machine Learning-Based Financial Literacy Assessment and Personalized Financial Education System"*

### Strict Methodological Boundary
The system maintains a strict separation between prediction, explanation, and educational recommendation:

```
ASSESSMENT (Responses & Performance Evidence)
   │
   ├───────────► ML ARCHETYPE PREDICTION (LightGBM_TOP75_ClassWeighted)
   │                  │
   │                  ▼
   │             ML Archetype (COMPETENT, OVERCONFIDENT, UNDERCONFIDENT, NAIVE)
   │                  │
   │                  ├───────────► SHAP EXPLANATION (Feature Contribution Analysis)
   │                  │
   ▼                  ▼
DETERMINISTIC EDUCATIONAL RULE ENGINE
   │
   ▼
EDUCATIONAL NEED PROFILE
   │
   ▼
PRIORITIZED EDUCATIONAL RECOMMENDATIONS
   │
   ▼
CURATED EDUCATIONAL CONTENT CATALOG
```

1. **Prediction Layer**: The LightGBM model predicts the user's financial literacy archetype based on 75 NFCS predictor variables.
2. **Explanation Layer**: TreeSHAP quantifies individual feature contributions toward the archetype prediction (display-only explanatory evidence).
3. **Education Engine**: A deterministic rule engine derives topic-specific educational needs and prioritized recommendations using objective assessment performance, the predicted archetype, and explicit pedagogical rules.

> [!IMPORTANT]
> **Methodological Rule**: SHAP values measure feature contribution to model outputs, NOT causality or educational need. SHAP is NEVER used directly as an educational recommendation rule.

---

## 2. Frozen ML Archetypes & Educational Directions

The four ML archetypes defined by the objective vs. subjective financial literacy matrix serve as high-level educational direction anchors:

| Archetype | Objective Knowledge | Subjective Knowledge | General Educational Direction |
| :--- | :--- | :--- | :--- |
| **COMPETENT** | HIGH | HIGH | Advanced reinforcement, deeper financial planning, investment/risk concepts, long-term decision making. |
| **OVERCONFIDENT** | LOW | HIGH | Foundational financial concepts, confidence calibration, validating financial assumptions, interest/inflation, risk/diversification, credit/debt fundamentals. |
| **UNDERCONFIDENT** | HIGH | LOW | Reinforcing demonstrated knowledge, confidence building, recognizing existing capability, practical application. |
| **NAIVE** | LOW | LOW | Foundational financial literacy: budgeting, saving, interest, inflation, credit, debt, risk, basic financial planning. |

### Preserved Communication Tone & Language Guidelines

- **COMPETENT**: *"Your results indicate relatively strong financial literacy capability. The recommended content focuses on reinforcing and extending that capability."*
- **OVERCONFIDENT**: *"Your assessment pattern suggests that your self-assessed financial knowledge is higher than your demonstrated performance on the assessment. The recommended content focuses on strengthening core concepts and validating financial assumptions."*
- **UNDERCONFIDENT**: *"Your assessment pattern suggests stronger demonstrated performance than your self-assessed financial knowledge. The recommended content focuses on reinforcing your existing knowledge and building confidence in applying it."*
- **NAIVE**: *"The assessment indicates opportunities to strengthen foundational financial concepts."*

---

## 3. Topic-Performance Calculation & Thresholds

For any completed assessment, topic-level performance is derived from answered questions grouped by `FinancialLiteracyCategory`.

$$\text{Topic Performance Percentage} = \frac{\text{Correct Answers in Topic}}{\text{Total Answered Questions in Topic}} \times 100\%$$

### Performance Level Thresholds

| Topic Score Range | Performance Level | Educational Need State |
| :--- | :--- | :--- |
| **0.0% – 39.99%** | `NEEDS_FOUNDATION` | Topic requires fundamental concept building. |
| **40.0% – 69.99%** | `NEEDS_REINFORCEMENT` | Topic requires reinforcement and core concept strengthening. |
| **70.0% – 100.0%** | `STRONG` | Topic demonstrates solid mastery; candidate for advanced topics or confidence building. |

> [!NOTE]
> If a category has **0 answered questions** in the assessment, it is treated as having *No Assessment Evidence* and does NOT trigger a weak performance recommendation.

---

## 4. Educational Taxonomy

The education engine supports 11 domain topics mapped to the `FinancialLiteracyCategory` enum:

1. **BUDGETING**: Income, expense tracking, cash flow management, needs vs. wants.
2. **SAVING**: Emergency funds, short/long term goals, liquidity management.
3. **CREDIT**: Credit scores, credit utilization, responsible borrowing habits.
4. **DEBT_MANAGEMENT**: Interest rates on debt, payoff strategies, managing liabilities.
5. **INVESTING**: Asset allocation, risk-return trade-offs, compounding over time.
6. **INSURANCE**: Risk transfer, emergency protection, coverage types.
7. **FINANCIAL_PLANNING**: Goal setting, retirement planning, holistic financial health.
8. **INTEREST**: Simple vs. compound interest calculations and time value of money.
9. **INFLATION**: Purchasing power degradation, real vs. nominal returns.
10. **RISK**: Diversification, volatility, risk tolerance assessment.
11. **TAXATION**: Tax awareness (recommended only when explicit assessment evidence is present).

---

## 5. Recommendation Priority & Recommendation Types

### Recommendation Priority Rules

Recommendations receive a deterministic priority (`HIGH`, `MEDIUM`, `LOW`) based on topic performance and archetype context:

- **`HIGH` Priority**:
  - Any topic with `NEEDS_FOUNDATION` (< 40% performance).
  - Core foundational topics (BUDGETING, SAVING, CREDIT, INTEREST, INFLATION) for `NAIVE` and `OVERCONFIDENT` archetypes.
- **`MEDIUM` Priority**:
  - Any topic with `NEEDS_REINFORCEMENT` (40%–69% performance).
  - Topics where `UNDERCONFIDENT` users scored `STRONG` (targeted for confidence building).
- **`LOW` Priority**:
  - Topics with `STRONG` (>= 70%) performance for `COMPETENT` or `NAIVE` users (offered as advanced or optional reinforcement).

### Recommendation Type Definitions

- `FOUNDATION`: Building core fundamental concepts for weak topics (< 40%).
- `REINFORCEMENT`: Strengthening moderate performance topics (40–69%).
- `CONFIDENCE_BUILDING`: Reinforcing demonstrated knowledge for `UNDERCONFIDENT` users who scored well (>= 70%).
- `ADVANCED`: Extending concepts for `COMPETENT` users with strong topic performance.
- `PRACTICAL_APPLICATION`: Practical tools and application exercises.

---

## 6. Deterministic Ordering and Deduplication

Recommendations are ordered strictly deterministically:
1. **Priority**: `HIGH` > `MEDIUM` > `LOW`
2. **Topic Score**: Ascending (weakest topics first)
3. **Topic Name**: Alphabetical ascending (`FinancialLiteracyCategory.name()`)
4. **Content Title**: Alphabetical ascending

Repeated API requests for the same completed assessment produce identical recommendation ordering and content.

---

## 7. Educational Content Storage Schema

Curated educational content is persisted in PostgreSQL/H2 via Flyway migration `V4__create_education_content_schema.sql` in table `educational_contents`:

```sql
CREATE TABLE educational_contents (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    topic VARCHAR(50) NOT NULL,
    difficulty VARCHAR(50) NOT NULL,
    short_description TEXT NOT NULL,
    learning_objectives TEXT NOT NULL,
    content TEXT NOT NULL,
    estimated_duration_minutes INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_educational_contents_topic ON educational_contents(topic);
```

---

## 8. REST API Specification

### Endpoint
`GET /api/assessments/{assessmentId}/education`

### Success Response (HTTP 200 OK)
```json
{
  "assessmentId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "archetype": "UNDERCONFIDENT",
  "archetypeSummary": "Your assessment pattern suggests stronger demonstrated performance than your self-assessed financial knowledge. The recommended content focuses on reinforcing your existing knowledge and building confidence in applying it.",
  "generatedAt": "2026-10-04T21:45:00Z",
  "totalRecommendations": 3,
  "recommendations": [
    {
      "recommendationId": "a1b2c3d4-e5f6-7890-1234-56789abcdef0",
      "contentId": "d7b23114-1111-4a2b-8888-000000000001",
      "topic": "SAVING",
      "priority": "HIGH",
      "recommendationType": "CONFIDENCE_BUILDING",
      "title": "Emergency Savings Fundamentals",
      "shortDescription": "Learn how to establish and maintain an emergency fund.",
      "rationale": "Your demonstrated performance in saving-related questions indicates strong concept knowledge. This lesson reinforces your understanding to build confidence.",
      "learningObjective": "Understand emergency fund calculation and liquidity management.",
      "estimatedDurationMinutes": 15,
      "difficulty": "BEGINNER"
    }
  ]
}
```

### Error Responses
- `HTTP 404 NOT FOUND`: Assessment ID does not exist.
- `HTTP 400 BAD REQUEST`: Assessment is incomplete or has no answered questions.
- `HTTP 503 SERVICE UNAVAILABLE`: ML prediction is unavailable for the assessment.

---

## 9. Safety and Financial Advice Boundary

> [!CAUTION]
> **Safety Boundary**: The Personalized Financial Education Engine provides educational content ONLY. It MUST NOT provide individualized financial advice, stock recommendations, or specific financial product endorsements.

The system will NEVER generate recommendations such as:
- *"Buy stock XYZ."*
- *"Invest 20% of your income in fund ABC."*
- *"Take out a personal loan."*
- *"Purchase insurance policy 123."*

All generated content and rationales are strictly general financial literacy education.

---

## 10. Future AI Enhancement Boundary

In future phases (Phase 7E+), generative AI (e.g., LLMs) may optionally be used downstream of the deterministic education engine to rephrase content or answer user follow-up questions. 

However, **AI must remain strictly downstream** of the deterministic recommendation logic. The recommendation rules, topic performance calculations, and priority ordering will always remain deterministic.
