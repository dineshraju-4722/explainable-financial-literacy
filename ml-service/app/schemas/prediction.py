from pydantic import BaseModel, Field
from typing import Dict, Any, List, Optional

class PredictionRequest(BaseModel):
    features: Dict[str, Any] = Field(
        ...,
        description="Dictionary mapping NFCS original variable names (e.g. M1_1, J20, J1) to response values",
        example={
            "M1_1": "6",
            "J20": "8",
            "J1": "1",
            "J32": "2",
            "J8": "1",
            "A50A": "1",
            "A14": "1",
            "B60": "1"
        }
    )

class PredictionResponse(BaseModel):
    archetype: str = Field(..., description="Predicted Financial Literacy Archetype (COMPETENT, OVERCONFIDENT, UNDERCONFIDENT, NAIVE)")
    probabilities: Dict[str, float] = Field(..., description="Probability breakdown across all 4 archetypes")
    model_version: str = Field(..., description="Model version identifier")
    timestamp: str = Field(..., description="ISO 8601 prediction timestamp")
    metadata: Optional[Dict[str, Any]] = Field(None, description="Model metadata and performance reference metrics")

class FeatureContributor(BaseModel):
    feature: str = Field(..., description="Original NFCS predictor variable code")
    description: str = Field(..., description="Questionnaire description of the variable")
    shap_value: float = Field(..., description="Aggregated signed SHAP value contribution")

class ExplanationData(BaseModel):
    target_class: str = Field(..., description="Archetype class target explained")
    top_k: int = Field(..., description="Number of top positive and negative contributors returned")
    top_positive_contributors: List[FeatureContributor] = Field(..., description="Features with strongest positive SHAP contribution toward target_class")
    top_negative_contributors: List[FeatureContributor] = Field(..., description="Features with strongest negative SHAP contribution away from target_class")

class ExplanationRequest(BaseModel):
    features: Dict[str, Any] = Field(..., description="Dictionary mapping 75 NFCS original variable names to response values")
    target_class: Optional[str] = Field(None, description="Optional target archetype class to explain (COMPETENT, OVERCONFIDENT, UNDERCONFIDENT, NAIVE)")
    top_k: Optional[int] = Field(5, description="Number of top positive/negative feature contributors (1 to 10, default 5)")

class ExplanationResponse(BaseModel):
    archetype: str = Field(..., description="Predicted Financial Literacy Archetype")
    probabilities: Dict[str, float] = Field(..., description="Probability breakdown across all 4 archetypes")
    model_version: str = Field(..., description="Model version identifier")
    timestamp: str = Field(..., description="ISO 8601 timestamp")
    explanation: ExplanationData = Field(..., description="Instance-level TreeSHAP explanation details")
