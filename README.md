# Explainable Machine Learning-Based Financial Literacy Assessment and Personalized Financial Education System

An academic research platform extending the foundational research of **Alex Yue Feng Zhu (2025)** (*Technology in Society*, DOI: [10.1016/j.techsoc.2024.102797](https://doi.org/10.1016/j.techsoc.2024.102797)).

---

## 📌 Research Overview & Motivation

While the reference study uses supervised machine learning (Decision Tree, Random Forest, LightGBM, SVM) to classify financial literacy status among youth, our system extends this framework into an interactive educational feedback loop:

1. **Weak-Area Identification**: Granular detection across 10 financial domains (Saving, Budgeting, Interest, Compound Interest, Inflation, Debt, Investment, Risk, Insurance, Financial Decision-Making).
2. **Personalized Education**: Target rule-based educational modules tailored to explicit user knowledge gaps.
3. **Explainable AI (SHAP)**: Human-interpretable explanations detailing key features influencing classification without exposing raw metrics to lay users.
4. **Reassessment & Longitudinal Progress**: Quantitative measurement of knowledge gain in percentage points.

> **Research Scope Statement**: The system strictly assesses financial knowledge and behavior to guide targeted education. It does NOT predict academic eligibility, scholarship status, financial need, or poverty.

---

## 🏗 System Architecture

```
[ React + Vite Frontend ] (Port 3000)
         │
         ▼
[ Spring Boot REST API Backend ] (Port 8080)
         │                         │
         ▼                         ▼
 [ PostgreSQL DB ]       [ Python FastAPI ML Service ] (Port 8000)
   (Port 5432)             (scikit-learn, LightGBM, SHAP)
```

---

## 📁 Repository Structure

```
.
├── frontend/             # React + Vite web application
├── backend/              # Spring Boot 3.x REST backend (Java 21, Gradle)
├── ml-service/           # Python FastAPI Machine Learning service
├── research/             # Datasets, Jupyter notebooks, experiments, figures & reports
│   ├── dataset/          # Raw and preprocessed anonymized survey data
│   ├── notebooks/        # Model training, hyperparameter tuning & SHAP analysis
│   ├── experiments/      # Model comparison logs and cross-validation outputs
│   ├── results/          # Metric evaluation tables (Accuracy, F1, Precision, Recall)
│   ├── figures/          # SHAP summary plots, confusion matrices
│   └── reports/          # Academic progress reports & project drafts
├── docs/                 # System design & API documentation
│   ├── architecture/     # Component design and data flow diagrams
│   ├── api/              # OpenAPI / REST endpoint specs
│   └── research/         # Research methodology and ethical compliance details
├── docker/               # Container configurations
├── docker-compose.yml    # Full-stack Docker setup
├── .env.example          # Environment variable template
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites
- Docker Engine 24+ & Docker Compose v2+
- **Optionally for local development without Docker**:
  - JDK 21 LTS
  - Python 3.11+
  - Node.js 18+
  - PostgreSQL 16+

### Quick Start (Docker Compose)
1. Clone the repository and navigate to the project root:
   ```bash
   cp .env.example .env
   ```
2. Build and launch all containerized services:
   ```bash
   docker-compose up --build -d
   ```
3. Check service health:
   - **Frontend**: `http://localhost:3000`
   - **Spring Boot Backend Health**: `http://localhost:8080/api/health`
   - **Python ML Service Health**: `http://localhost:8000/health`

---

## 🧪 Phase Plan Roadmap

- [x] **Phase 1: Project Skeleton, Services & Docker Setup** *(Current)*
- [ ] **Phase 2: PostgreSQL Schema & Spring Boot Configuration**
- [ ] **Phase 3: Authentication & Authorization (JWT + Roles)**
- [ ] **Phase 4: Questionnaire & Assessment Engine**
- [ ] **Phase 5: Dataset Export & ML Preprocessing Pipeline**
- [ ] **Phase 6: Baseline ML Model Training & Stratified 5-Fold Cross Validation**
- [ ] **Phase 7: Spring Boot <-> Python ML Prediction Bridge**
- [ ] **Phase 8: Rule-Based Weak-Area Detection Engine**
- [ ] **Phase 9: Personalized Educational Content & Recommendation Engine**
- [ ] **Phase 10: Reassessment Engine & Longitudinal Progress Tracker**
- [ ] **Phase 11: SHAP Model Explainability Engine & User Translation Layer**
- [ ] **Phase 12: End-to-End Integration, Security Audit & Academic Paper Assets**

---

## 🛡 License & Citation

Reference Research Paper:
> Zhu, A. Y. F. (2025). Unlocking financial literacy with machine learning: A critical step to advance personal finance research and practice. *Technology in Society*, 102797. DOI: 10.1016/j.techsoc.2024.102797
