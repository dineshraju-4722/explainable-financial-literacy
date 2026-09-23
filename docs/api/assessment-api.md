# Assessment & Questionnaire Engine API Specification

## 1. Overview

The **Assessment Engine API** manages users, question bank records, assessment initialization, answer submission, transactional completion, and score retrieval.

All endpoints return a uniform response envelope:

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": "2026-09-23T10:40:00Z"
}
```

Error responses return HTTP 400, 404, 409, or 500 status codes:

```json
{
  "success": false,
  "message": "Error details...",
  "errors": [],
  "timestamp": "2026-09-23T10:40:00Z"
}
```

---

## 2. Participant Management (`/api/users`)

### Create User
- **`POST /api/users`**
- **Request Body**:
  ```json
  {
    "name": "Jane Doe",
    "email": "jane.doe@university.edu",
    "age": 21,
    "educationLevel": "Undergraduate",
    "occupation": "Student"
  }
  ```
- **Response**: `210 Created`

### Retrieve User
- **`GET /api/users/{id}`**

---

## 3. Question Bank Management (`/api/questions`)

### Create Question
- **`POST /api/questions`**
- **Validation Rules**:
  - `MULTIPLE_CHOICE`: Requires ≥2 options, exactly 1 option with `isCorrect = true`.
  - `TRUE_FALSE`: Requires exactly 2 options, exactly 1 option with `isCorrect = true`.
- **Request Body**:
  ```json
  {
    "questionText": "Which budget category should emergency savings belong to?",
    "category": "SAVING",
    "difficulty": "BEGINNER",
    "questionType": "MULTIPLE_CHOICE",
    "options": [
      { "optionText": "Needs", "optionOrder": 1, "isCorrect": true },
      { "optionText": "Wants", "optionOrder": 2, "isCorrect": false }
    ]
  }
  ```

### Filter Questions
- **`GET /api/questions`**
- **Query Parameters**:
  - `category`: `BUDGETING`, `SAVING`, `INTEREST`, etc.
  - `difficulty`: `BEGINNER`, `INTERMEDIATE`, `ADVANCED`
  - `questionType`: `MULTIPLE_CHOICE`, `TRUE_FALSE`
  - `active`: `true` / `false`

### Security Rule
- **`GET /api/questions` and `GET /api/questions/{id}`** return:
  ```json
  {
    "id": "...",
    "questionText": "...",
    "category": "SAVING",
    "difficulty": "BEGINNER",
    "questionType": "MULTIPLE_CHOICE",
    "options": [
      { "id": "...", "optionText": "Needs", "optionOrder": 1 }
    ]
  }
  ```
  *Notice: `isCorrect` is strictly omitted from all public responses.*

---

## 4. Assessment Lifecycle (`/api/assessments`)

### Initialize Assessment
- **`POST /api/assessments`**
- **Request Body**:
  ```json
  {
    "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "assessmentType": "INITIAL"
  }
  ```

### Retrieve Assessment Questions
- **`GET /api/assessments/{id}/questions`**
- Returns list of active questions with client-safe options.

### Submit / Update Answer
- **`POST /api/assessments/{id}/responses`**
- **Request Body**:
  ```json
  {
    "questionId": "...",
    "selectedOptionId": "..."
  }
  ```
- **Upsert Behavior**: Submitting a response for a question already answered updates the existing response row rather than inserting duplicate records.

### Complete Assessment
- **`POST /api/assessments/{id}/complete`**
- Transactionally scores responses, sets status to `COMPLETED`, updates `completedAt`, and persists `AssessmentResult`.

### Retrieve Result
- **`GET /api/assessments/{id}/result`**
- **Response Body**:
  ```json
  {
    "assessmentId": "...",
    "totalQuestions": 10,
    "correctAnswers": 8,
    "totalScore": 8.0,
    "percentage": 80.0,
    "proficiencyLevel": "ADVANCED",
    "calculatedAt": "2026-09-23T10:40:00Z"
  }
  ```
