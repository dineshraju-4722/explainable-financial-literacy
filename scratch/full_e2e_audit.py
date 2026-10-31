import json
import urllib.request
import urllib.error

BASE_URL = "http://localhost:8081/api"
USER_ID = "3fa85f64-5717-4562-b3fc-2c963f66afa6"

def make_req(method, endpoint, body=None):
    url = f"{BASE_URL}{endpoint}"
    data = json.dumps(body).encode('utf-8') if body else None
    headers = {"Content-Type": "application/json"} if data else {}
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req) as resp:
            res_text = resp.read().decode('utf-8')
            return resp.status, json.loads(res_text)
    except urllib.error.HTTPError as e:
        res_text = e.read().decode('utf-8')
        return e.code, json.loads(res_text)

def run_audit():
    print("=== STEP 1: CREATE ASSESSMENT ===")
    status, res = make_req("POST", "/assessments", {"userId": USER_ID, "assessmentType": "INITIAL"})
    print(f"Status: {status}")
    assessment_id = res["data"]["id"]
    print(f"Assessment ID: {assessment_id}")

    print("\n=== STEP 2: FETCH QUESTIONS ===")
    status, res = make_req("GET", f"/assessments/{assessment_id}/questions")
    questions = res["data"]
    print(f"Retrieved {len(questions)} questions")

    print("\n=== STEP 3: SUBMIT RESPONSES ===")
    for q in questions:
        q_id = q["id"]
        opt_id = q["options"][0]["id"]
        status, resp = make_req("POST", f"/assessments/{assessment_id}/responses", {
            "questionId": q_id,
            "selectedOptionId": opt_id
        })
        print(f"Submitted answer for question {q_id[:8]}: Status {status}")

    print("\n=== STEP 4: COMPLETE ASSESSMENT ===")
    status, res = make_req("POST", f"/assessments/{assessment_id}/complete")
    print(f"Status: {status}")
    result = res["data"]
    print(json.dumps(result, indent=2))

    print("\n=== STEP 5: FETCH RESULT ===")
    status, res = make_req("GET", f"/assessments/{assessment_id}/result")
    print(f"Status: {status}")

    print("\n=== STEP 6: FETCH ANALYTICS ===")
    status, res = make_req("GET", f"/assessments/{assessment_id}/analytics")
    print(f"Status: {status}")
    analytics = res["data"]

    print("\n=== STEP 7: SHAP EXPLANATION AUDIT ===")
    pred_archetype = result.get("mlArchetype")
    print(f"Predicted Archetype: {pred_archetype}")
    
    # Flatten feature vector for SHAP request
    flat_features = {}
    if analytics.get("featureVector") and analytics["featureVector"].get("categoryFeatures"):
        for cat, val in analytics["featureVector"]["categoryFeatures"].items():
            flat_features[f"cat_{cat}"] = str(val.get("percentage") or 0)
    flat_features["overall_percentage"] = str(analytics.get("overallPercentage") or 0)

    status, shap_res = make_req("POST", "/ml/explain", {
        "features": flat_features,
        "target_class": pred_archetype,
        "top_k": 5
    })
    print(f"SHAP API Status: {status}")
    print(json.dumps(shap_res, indent=2))
    
    data = shap_res.get("data", {})
    target_class_returned = data.get("target_class") or data.get("targetClass")
    print(f"Target Class Returned by SHAP: {target_class_returned}")
    assert target_class_returned == pred_archetype, f"SHAP MISMATCH: Predicted {pred_archetype} != SHAP target {target_class_returned}"

    print("\n=== STEP 8: FETCH EDUCATION RECOMMENDATIONS ===")
    status, edu_res = make_req("GET", f"/assessments/{assessment_id}/education")
    print(f"Status: {status}")
    print(json.dumps(edu_res, indent=2))
    
    recommendations = edu_res["data"].get("recommendations", [])
    if recommendations:
        content_id = recommendations[0]["contentId"]
        print(f"\n=== STEP 9: FETCH EDUCATION CONTENT DETAIL (ID: {content_id}) ===")
        status, content_res = make_req("GET", f"/education/content/{content_id}")
        print(f"Status: {status}")
        print(json.dumps(content_res, indent=2))

if __name__ == "__main__":
    run_audit()
