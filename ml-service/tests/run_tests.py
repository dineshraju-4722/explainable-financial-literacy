import sys
import os

script_dir = os.path.dirname(os.path.abspath(__file__))
ml_service_dir = os.path.abspath(os.path.join(script_dir, ".."))
if ml_service_dir not in sys.path:
    sys.path.insert(0, ml_service_dir)

from fastapi.testclient import TestClient
from app.main import app

def main():
    print("=" * 70)
    print("RUNNING FASTAPI ML SERVICE TEST SUITE (PREDICTION + TREESHAP EXPLAIN)")
    print("=" * 70)

    with TestClient(app) as client:
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

        # 1. Health check
        print("\n1. Testing GET /health ...", end=" ")
        res = client.get("/health")
        assert res.status_code == 200, f"Expected 200, got {res.status_code}"
        data = res.json()
        assert data["status"] == "UP"
        assert data["service"] == "ml-service"
        assert data["models_status"]["trained"] is True
        print("PASSED [OK]")

        # 2. Predict endpoint
        print("2. Testing POST /predict ...", end=" ")
        res = client.post("/predict", json=payload)
        assert res.status_code == 200, f"Expected 200, got {res.status_code}"
        pred = res.json()
        assert pred["archetype"] in ["COMPETENT", "OVERCONFIDENT", "UNDERCONFIDENT", "NAIVE"]
        assert len(pred["probabilities"]) == 4
        print(f"PASSED [OK] (Predicted: {pred['archetype']})")

        # 3. Explain endpoint & Consistency test
        print("3. Testing POST /explain & Prediction Consistency ...", end=" ")
        res_exp = client.post("/explain", json=payload)
        assert res_exp.status_code == 200, f"Expected 200, got {res_exp.status_code}"
        exp = res_exp.json()
        
        # Verify Prediction/Explanation Consistency (Exact match for archetype & probas within 0.0001)
        assert exp["archetype"] == pred["archetype"], f"Archetype mismatch: {exp['archetype']} vs {pred['archetype']}"
        for c in pred["probabilities"]:
            diff = abs(exp["probabilities"][c] - pred["probabilities"][c])
            assert diff < 0.0001, f"Probability discrepancy for {c}: {exp['probabilities'][c]} vs {pred['probabilities'][c]}"

        # Verify explanation structure
        explanation_data = exp["explanation"]
        assert explanation_data["target_class"] == pred["archetype"]
        assert explanation_data["top_k"] == 5
        assert len(explanation_data["top_positive_contributors"]) <= 5
        assert len(explanation_data["top_negative_contributors"]) <= 5

        for item in explanation_data["top_positive_contributors"]:
            assert "feature" in item
            assert "description" in item
            assert "shap_value" in item
            assert item["shap_value"] > 0

        for item in explanation_data["top_negative_contributors"]:
            assert "feature" in item
            assert "description" in item
            assert "shap_value" in item
            assert item["shap_value"] < 0

        print(f"PASSED [OK] (Target Class: {explanation_data['target_class']}, Top Pos: {len(explanation_data['top_positive_contributors'])}, Top Neg: {len(explanation_data['top_negative_contributors'])})")

        # 4. Top-K limit parameter test (top_k=3)
        print("4. Testing POST /explain with top_k=3 ...", end=" ")
        payload_k3 = dict(payload)
        payload_k3["top_k"] = 3
        res_k3 = client.post("/explain", json=payload_k3)
        assert res_k3.status_code == 200
        exp_k3 = res_k3.json()["explanation"]
        assert exp_k3["top_k"] == 3
        assert len(exp_k3["top_positive_contributors"]) <= 3
        assert len(exp_k3["top_negative_contributors"]) <= 3
        print("PASSED [OK]")

        # 5. Invalid target_class validation
        print("5. Testing POST /explain invalid target_class ...", end=" ")
        payload_inv = dict(payload)
        payload_inv["target_class"] = "INVALID_CLASS"
        res_inv = client.post("/explain", json=payload_inv)
        assert res_inv.status_code == 400
        print("PASSED [OK]")

        # 6. Invalid top_k validation
        print("6. Testing POST /explain invalid top_k ...", end=" ")
        payload_inv_k = dict(payload)
        payload_inv_k["top_k"] = 25
        res_inv_k = client.post("/explain", json=payload_inv_k)
        assert res_inv_k.status_code == 400
        print("PASSED [OK]")

    print("\nALL FASTAPI PREDICTION + TREESHAP EXPLANATION TESTS PASSED CLEANLY!")

if __name__ == "__main__":
    main()
