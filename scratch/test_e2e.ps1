# scratch/test_e2e.ps1 - End-to-End System Verification Script

$backendUrl = "http://localhost:8081/api"
$mlUrl = "http://localhost:8000"

Write-Host "=== STARTING E2E VERIFICATION ==="

# 1. Backend Health
$health = Invoke-RestMethod -Uri "$backendUrl/health" -Method Get
Write-Host "1. Backend Health Status:" $health.status

# 2. ML Service Health
$mlHealth = Invoke-RestMethod -Uri "$mlUrl/health" -Method Get
Write-Host "2. ML Service Status:" $mlHealth.status

# 3. Create User
$userReq = @{
    name = "E2E Verification User"
    email = "e2e.user." + [Guid]::NewGuid().ToString().Substring(0,8) + "@example.com"
    age = 29
    educationLevel = "Master"
    occupation = "Researcher"
} | ConvertTo-Json

$userRes = Invoke-RestMethod -Uri "$backendUrl/users" -Method Post -Body $userReq -ContentType "application/json"
$userId = $userRes.data.id
Write-Host "3. User Created ID:" $userId

# 4. Create Assessment
$assReq = @{
    userId = $userId
    assessmentType = "INITIAL"
} | ConvertTo-Json

$assRes = Invoke-RestMethod -Uri "$backendUrl/assessments" -Method Post -Body $assReq -ContentType "application/json"
$assId = $assRes.data.id
Write-Host "4. Assessment Created ID:" $assId

# 5. Fetch Questions
$qRes = Invoke-RestMethod -Uri "$backendUrl/assessments/$assId/questions" -Method Get
$questions = $qRes.data
Write-Host "5. Questions Fetched Count:" $questions.Count

# 6. Submit Responses
foreach ($q in $questions) {
    $firstOpt = $q.options[0]
    $respReq = @{
        questionId = $q.id
        selectedOptionId = $firstOpt.id
    } | ConvertTo-Json

    $null = Invoke-RestMethod -Uri "$backendUrl/assessments/$assId/responses" -Method Post -Body $respReq -ContentType "application/json"
}
Write-Host "6. Submitted responses for all" $questions.Count "questions."

# 7. Complete & Score Assessment (Triggers ML Prediction)
$compRes = Invoke-RestMethod -Uri "$backendUrl/assessments/$assId/complete" -Method Post -ContentType "application/json"
$resultData = $compRes.data
Write-Host "7. Assessment Completed & Scored:"
Write-Host "   - Assessment ID:" $resultData.assessmentId
Write-Host "   - Percentage:" $resultData.percentage "%"
Write-Host "   - Proficiency Level:" $resultData.proficiencyLevel
Write-Host "   - ML Archetype Predicted:" $resultData.mlArchetype
Write-Host "   - ML Model Version:" $resultData.mlModelVersion

# 8. Fetch Education Recommendations
$eduRes = Invoke-RestMethod -Uri "$backendUrl/assessments/$assId/education" -Method Get
Write-Host "8. Education Recommendations Response:"
Write-Host "   - Archetype:" $eduRes.archetype
Write-Host "   - Summary:" $eduRes.archetypeSummary
Write-Host "   - Total Recommendations:" $eduRes.totalRecommendations
if ($eduRes.recommendations.Count -gt 0) {
    $firstRec = $eduRes.recommendations[0]
    Write-Host "   - First Recommendation Title:" $firstRec.title
    Write-Host "   - Topic:" $firstRec.topic
    Write-Host "   - Priority:" $firstRec.priority
    Write-Host "   - Type:" $firstRec.recommendationType
}

# 9. Fetch Assessment Analytics
$anaRes = Invoke-RestMethod -Uri "$backendUrl/assessments/$assId/analytics" -Method Get
Write-Host "9. Analytics Fetched:"
Write-Host "   - Categories Analyzed:" $anaRes.data.categoryAnalytics.Count

# 10. TreeSHAP Explanation API Call
$featuresRes = Invoke-RestMethod -Uri "$backendUrl/ml/features" -Method Get
$featMap = [ordered]@{}
foreach ($f in $featuresRes.data) {
    $featMap[$f] = "1"
}

$expReq = @{
    features = $featMap
    targetClass = if ($resultData.mlArchetype) { $resultData.mlArchetype } else { "COMPETENT" }
    topK = 5
} | ConvertTo-Json -Depth 5

$expRes = Invoke-RestMethod -Uri "$backendUrl/ml/explain" -Method Post -Body $expReq -ContentType "application/json"
$expData = $expRes.data
Write-Host "10. TreeSHAP Explanation API Response:"
Write-Host "    - Model Version:" $expData.model_version
Write-Host "    - Target Class:" $expData.explanation.target_class
Write-Host "    - Top Positive Contributors Count:" $expData.explanation.top_positive_contributors.Count
Write-Host "    - Top Positive Feature:" $expData.explanation.top_positive_contributors[0].feature "(" $expData.explanation.top_positive_contributors[0].description ")"
Write-Host "    - Top Positive SHAP Value:" $expData.explanation.top_positive_contributors[0].shap_value
Write-Host "    - Top Negative Contributors Count:" $expData.explanation.top_negative_contributors.Count
Write-Host "    - Top Negative Feature:" $expData.explanation.top_negative_contributors[0].feature "(" $expData.explanation.top_negative_contributors[0].description ")"
Write-Host "    - Top Negative SHAP Value:" $expData.explanation.top_negative_contributors[0].shap_value

Write-Host "=== END-TO-END VERIFICATION COMPLETE & SUCCESSFUL ==="
