# 11. System Architecture & Research/Application Separation

## Full-Stack Docker Container Architecture

```
                          Docker Compose Environment
                                      │
         ┌────────────────────────────┼────────────────────────────┐
         │                            │                            │
         ▼                            ▼                            ▼
   PostgreSQL 16                 Spring Boot 3.2.3             FastAPI ML Service
   (port 5432:5432)              (port 8081:8080)               (port 8000:8000)
   Container: fl_postgres        Container: fl_backend          Container: fl_ml_service
   Volume: postgres_data         (Flyway V1-V5 Schema)          (LightGBM v1.0.0 + TreeSHAP)
         │                            │                            │
         │                            ├────────────────────────────┤
         │                            │  (ml-service:8000/predict) │
         │                            ▼                            ▼
         │                       LightGBM Model                 TreeSHAP Explainer
         │                            │                            │
         └────────────────────────────┼────────────────────────────┘
                                      │
                                      ▼
                             React 18 Dashboard
                             Container: fl_frontend
                              (port 3000:80)
```

---

## Research vs. Application Separation

1. **Research Model Foundation**: The LightGBM classifier (`v1.0.0`) was trained, validated, and optimized exclusively on the FINRA NFCS 2024 microdata ($N = 25,030$).
2. **Application Assessment Questions**: Operational web application questions (seeded via Flyway `V5__seed_questions.sql`) are pedagogical user-engagement items.
3. **Operational Database Isolation**: Flyway `V5` seeds application runtime data in PostgreSQL. No operational seed questions were used during machine learning model training, and no research microdata was altered during application development.
