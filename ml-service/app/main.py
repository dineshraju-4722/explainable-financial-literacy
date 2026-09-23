from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from datetime import datetime, timezone
import os

app = FastAPI(
    title="Financial Literacy ML Service",
    description="Machine Learning service for Financial Literacy prediction and SHAP explainability",
    version="1.0.0"
)

# Enable CORS for backend & local development
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/health")
def health_check():
    """
    Health check endpoint exposing operational status and service metadata.
    """
    return {
        "status": "UP",
        "service": "ml-service",
        "version": "1.0.0",
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "environment": os.getenv("ENVIRONMENT", "development"),
        "models_status": {
            "trained": False,
            "available_models": ["DecisionTree", "RandomForest", "LightGBM", "SVM"]
        }
    }

if __name__ == "__main__":
    import uvicorn
    port = int(os.getenv("PORT", 8000))
    uvicorn.run("app.main:app", host="0.0.0.0", port=port, reload=True)
