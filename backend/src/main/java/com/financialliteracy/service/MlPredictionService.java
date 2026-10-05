package com.financialliteracy.service;

import com.financialliteracy.dto.assessment.AssessmentResultResponse;
import com.financialliteracy.dto.ml.MlExplanationResponse;
import com.financialliteracy.dto.ml.MlPredictionResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface MlPredictionService {

    List<String> getRequiredTop75Features();

    List<String> validateFeatureCompleteness(Map<String, String> features);

    MlPredictionResponse predictArchetype(Map<String, String> features);

    MlExplanationResponse explainArchetype(Map<String, String> features, String targetClass, Integer topK);

    AssessmentResultResponse predictAndStoreAssessmentArchetype(UUID assessmentId, Map<String, String> features);
}
