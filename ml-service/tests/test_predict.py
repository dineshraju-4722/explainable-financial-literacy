import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)

def test_health_endpoint():
    response = client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "UP"
    assert data["service"] == "ml-service"
    assert data["models_status"]["trained"] is True
    assert data["models_status"]["orig_predictors_count"] == 75
    assert data["models_status"]["encoded_features_count"] == 503

def test_predict_endpoint_success():
    payload = {
        "features": {
            "M1_1": "7",
            "J20": "9",
            "J1": "1",
            "J32": "1",
            "J8": "1",
            "A50A": "1",
            "A14": "1",
            "B60": "1"
        }
    }
    response = client.post("/predict", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert "archetype" in data
    assert data["archetype"] in ["COMPETENT", "OVERCONFIDENT", "UNDERCONFIDENT", "NAIVE"]
    assert "probabilities" in data
    assert len(data["probabilities"]) == 4
    assert "COMPETENT" in data["probabilities"]
    assert "OVERCONFIDENT" in data["probabilities"]
    assert "UNDERCONFIDENT" in data["probabilities"]
    assert "NAIVE" in data["probabilities"]
    assert data["model_version"] == "1.0.0"
    assert "timestamp" in data

def test_explain_endpoint():
    payload = {
        "features": {
            "M1_1": "2",
            "J20": "2",
            "J1": "7",
            "J32": "5"
        }
    }
    response = client.post("/explain", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert "archetype" in data
    assert "probabilities" in data
    assert data["explanation_status"] == "AVAILABLE"
