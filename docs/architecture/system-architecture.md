# System Architecture Specification

## Overview

The **Explainable Machine Learning-Based Financial Literacy Assessment and Personalized Financial Education System** follows a decoupled multi-tier architecture designed for scalability, maintainability, and scientific reproducibility.

## System Topology

```
+-----------------------------------------------------------------------+
|                              REACT FRONTEND                           |
|                         (Vite / JSX / Vanilla CSS)                    |
+-----------------------------------------------------------------------+
                                   |
                             REST / HTTP
                                   |
                                   v
+-----------------------------------------------------------------------+
|                           SPRING BOOT BACKEND                         |
|      - REST API Layer                                                 |
|      - Spring Security & JWT                                          |
|      - Weak-Area Analysis Engine                                      |
|      - Recommendation & Reassessment Engine                           |
|      - JPA Data Layer                                                 |
+-----------------------------------------------------------------------+
            |                                           |
      JDBC / SQL                                   REST / HTTP
            |                                           |
            v                                           v
+-----------------------+               +-------------------------------+
|     POSTGRESQL DB     |               |       PYTHON ML SERVICE       |
|  - Users & Roles      |               |  - Preprocessing & Pipelines  |
|  - Questions & Opts   |               |  - Decision Tree, RF, LightGBM|
|  - Assessments        |               |  - SVM Classifier             |
|  - Weak-Area Results  |               |  - SHAP Explainability API    |
|  - Progress Tracking  |               +-------------------------------+
+-----------------------+
```

## Service Responsibility Boundaries

### 1. Spring Boot Backend
- **Core Domain Authority**: Manages users, roles, assessment submissions, dynamic questions, and personalized content.
- **Weak-Area Engine**: Computes domain-level performance (e.g., 40% in Budgeting -> WEAK) independently of ML classification.
- **Orchestrator**: Acts as the single entry point for client applications. Calls the Python ML Service for inference and explanations.

### 2. Python ML Service (FastAPI)
- **Model Execution**: Loads trained Scikit-learn, LightGBM, and SVM pipelines.
- **Inference**: Computes binary or multi-class financial literacy predictions (`HIGH`, `LOW`, or 4-class mismatch profile).
- **SHAP Explanation**: Generates feature attribution values via `shap.TreeExplainer` or `KernelExplainer`.

### 3. PostgreSQL Database
- Relational schema enforcing referential integrity.
- Stores historical assessments for longitudinal progress tracking.

### 4. React Frontend
- User-facing UI built with responsive layout and glassmorphism styling.
- Renders assessment flows, category progress charts, and humanized SHAP explanations.
