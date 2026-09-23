# Domain Model Architecture & Database Schema Specification

## 1. Overview

The **Financial Literacy Platform** domain model provides an extensible relational persistence foundation built on PostgreSQL 16, Managed by Flyway DB Migrations (`V1__create_initial_domain_schema.sql`), and mapped via Spring Data JPA.

The domain strictly prioritizes data integrity, non-duplication of questions/options, and clean lifecycle boundaries.

---

## 2. Entity Overview & Identifier Strategy

### Primary Key Strategy: UUID (v4)
All entities utilize `java.util.UUID` with PostgreSQL `UUID` primary keys.
- **Rationale**: Prevents key enumeration attacks, allows client-side UUID generation when offline/batching, and eliminates single-sequence bottleneck across micro-services or distributed database nodes.

---

### Core Entities & Enums Summary

| Entity | Table Name | Key Purpose | Primary Relationships |
| :--- | :--- | :--- | :--- |
| **`User`** | `users` | Represents a participant/student | 1 ── N `Assessment` |
| **`Assessment`** | `assessments` | Tracks an evaluation attempt | N ── 1 `User`, 1 ── N `AssessmentResponse`, 1 ── 0..1 `AssessmentResult` |
| **`Question`** | `questions` | Categorized literacy question | 1 ── N `QuestionOption`, 1 ── N `AssessmentResponse` |
| **`QuestionOption`** | `question_options` | Choice item belonging to a question | N ── 1 `Question` |
| **`AssessmentResponse`** | `assessment_responses` | User's selected option per question | N ── 1 `Assessment`, N ── 1 `Question`, N ── 1 `QuestionOption` |
| **`AssessmentResult`** | `assessment_results` | Prepared scoring metrics table | 1 ── 1 (Unique FK) `Assessment` |

---

## 3. Enum Classification Systems

### `AssessmentStatus`
- `IN_PROGRESS`: Active assessment attempt.
- `COMPLETED`: Submitted and ready for weak-area scoring.
- `ABANDONED`: Timed out or dropped attempt.

### `AssessmentType`
- `INITIAL`: Baseline assessment prior to education.
- `REASSESSMENT`: Post-education measurement attempt to calculate percentage point growth.

### `FinancialLiteracyCategory`
- 11 core research domains: `BUDGETING`, `SAVING`, `CREDIT`, `INVESTING`, `INSURANCE`, `TAXATION`, `DEBT_MANAGEMENT`, `FINANCIAL_PLANNING`, `INTEREST`, `INFLATION`, `RISK`.

### `DifficultyLevel`
- `BEGINNER`, `INTERMEDIATE`, `ADVANCED`.

### `QuestionType`
- `MULTIPLE_CHOICE`, `TRUE_FALSE`.

---

## 4. Entity Relationship Design & Fetch Strategies

```
User (1) ────< (N) Assessment (1) ────< (N) AssessmentResponse (N) >──── (1) Question (1) ────< (N) QuestionOption
                       │
                     (0..1)
                       │
               AssessmentResult
```

### Relationship Rationale:
1. **Unidirectional Many-to-One Focus**: Entities reference their parent using `FetchType.LAZY` (e.g., `@ManyToOne(fetch = FetchType.LAZY) User user`). Bidirectional collections (e.g., `@OneToMany List<AssessmentResponse>`) are omitted on aggregate roots to prevent N+1 queries, memory bloat, and recursive serialization issues.
2. **Cascade Discipline**: `ON DELETE CASCADE` is enforced at the database layer (via Flyway migration scripts) rather than arbitrary JPA cascades, ensuring atomic database cleanup if a parent container is pruned.
3. **No Direct Option Duplication**: `AssessmentResponse` stores foreign keys to `Question` and `QuestionOption` (`selected_option_id`). Correct answers and option text are never copied into responses.

---

## 5. Schema Versioning via Flyway

- **Source of Truth**: Database schema modifications are exclusively executed via Flyway SQL scripts located in `classpath:db/migration/` (`V1__create_initial_domain_schema.sql`).
- **Hibernate Configuration**: Set to `spring.jpa.hibernate.ddl-auto: validate`. Hibernate only verifies that JPA entity metadata matches the schema created by Flyway. `ddl-auto: create` or `update` is disabled.

---

## 6. Forward Compatibility Support

The Phase 2 schema is explicitly designed to support future phases without premature implementation:
- **Phase 4 Scoring**: `AssessmentResult` table is present to store percentage scores and proficiency levels once scoring engines are integrated.
- **Phase 5 ML Feature Vectors**: Historical user assessments, categories, and behaviors can be extracted without modifying table structures.
- **Phase 7 Recommendation Engine**: `FinancialLiteracyCategory` enum maps directly to future learning content domain tags.
