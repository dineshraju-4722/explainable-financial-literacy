from fastapi import FastAPI, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from datetime import datetime, timezone
from contextlib import asynccontextmanager
import os

from app.services.model_service import model_service
from app.schemas.prediction import PredictionRequest, PredictionResponse, ExplanationRequest, ExplanationResponse

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup logic: Load ML model artifacts into memory
    try:
        model_service.load_artifacts()
    except Exception as e:
        print(f"[ERROR] Failed to load model artifacts on startup: {e}")
    yield
    # Shutdown logic (if needed)

app = FastAPI(
    title="Financial Literacy ML Service",
    description="Microservice for Financial Literacy Archetype Prediction (LightGBM TOP_75) and TreeSHAP Explainability",
    version="1.0.0",
    lifespan=lifespan
)

# Enable CORS for Spring Boot backend & local dev
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
    Health check endpoint exposing operational status, model availability, and metadata.
    """
    status_code = "UP" if model_service.is_loaded else "DEGRADED"
    return {
        "status": status_code,
        "service": "ml-service",
        "version": "1.0.0",
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "environment": os.getenv("ENVIRONMENT", "development"),
        "models_status": {
            "trained": model_service.is_loaded,
            "active_model": "LightGBM_TOP75_ClassWeighted",
            "orig_predictors_count": len(model_service.orig_predictors),
            "encoded_features_count": len(model_service.encoded_features),
            "metrics": model_service.metadata.get("metrics", {}) if model_service.is_loaded else {}
        }
    }

@app.post("/predict", response_model=PredictionResponse, status_code=status.HTTP_200_OK)
def predict_archetype(request: PredictionRequest):
    """
    Predict Financial Literacy Archetype (COMPETENT, OVERCONFIDENT, UNDERCONFIDENT, NAIVE) 
    given raw responses for the 75 NFCS predictor variables.
    """
    if not model_service.is_loaded:
        try:
            model_service.load_artifacts()
        except Exception as e:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail=f"Model service uninitialized and failed to load artifacts: {str(e)}"
            )

    try:
        result = model_service.predict(request.features)
        return PredictionResponse(**result)
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Inference error during archetype prediction: {str(e)}"
        )

@app.post("/explain", response_model=ExplanationResponse, status_code=status.HTTP_200_OK)
def explain_archetype(request: ExplanationRequest):
    """
    Instance-level TreeSHAP explanation endpoint. Aggregates 503 encoded SHAP contributions 
    back to original 75 NFCS variables and returns top positive & negative feature drivers.
    """
    if not model_service.is_loaded:
        try:
            model_service.load_artifacts()
        except Exception as e:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail=f"Model service uninitialized and failed to load artifacts: {str(e)}"
            )

    top_k = request.top_k if request.top_k is not None else 5
    if top_k < 1 or top_k > 10:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"top_k parameter must be an integer between 1 and 10, got {top_k}"
        )

    try:
        result = model_service.explain(
            features=request.features,
            target_class=request.target_class,
            top_k=top_k
        )
        return ExplanationResponse(**result)
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error generating TreeSHAP explanation: {str(e)}"
        )

if __name__ == "__main__":
    import uvicorn
    port = int(os.getenv("PORT", 8000))
    uvicorn.run("app.main:app", host="0.0.0.0", port=port, reload=True)
