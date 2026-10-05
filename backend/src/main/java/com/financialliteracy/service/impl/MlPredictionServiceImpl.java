package com.financialliteracy.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialliteracy.dto.assessment.AssessmentResultResponse;
import com.financialliteracy.dto.ml.MlPredictionRequest;
import com.financialliteracy.dto.ml.MlPredictionResponse;
import com.financialliteracy.entity.AssessmentResult;
import com.financialliteracy.exception.IncompleteFeatureVectorException;
import com.financialliteracy.exception.ResourceNotFoundException;
import com.financialliteracy.ml.MlPredictionClient;
import com.financialliteracy.repository.AssessmentResultRepository;
import com.financialliteracy.service.MlPredictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MlPredictionServiceImpl implements MlPredictionService {

    public static final List<String> TOP_75_FEATURES = List.of(
            "M1_1", "J20", "J1", "J32", "J8", "A50A", "A14", "B60", "J2", "B14A_1",
            "C10_2012", "A5_2015", "J43", "A50B", "C11_2012", "STATEQ", "C5_2012",
            "A3Ar_w", "A21_2015", "J33_40", "A8_2021", "C4_2012", "J63", "J41_3",
            "F2_3", "A41", "F2_6", "E20", "C3_2012", "G25_3", "G23",
            "M21_2_2015", "F2_5", "J41_1", "A4A_new_w", "B41_1", "B43", "B61", "J3",
            "F1", "B31", "G25_5", "A6", "J60_1", "G25_1", "F2_1", "A9", "P61", "J9",
            "C1_2012", "J6", "J42_1", "A11", "G25_4", "A7", "P60", "J41_2",
            "EA_1", "M20", "F2_2", "J5", "A40", "B41_2", "CENSUSDIV", "G1",
            "G25_2", "A10", "F2_4", "H30_3", "B1", "J42_2", "J4", "J64", "G35", "J62"
    );

    private final MlPredictionClient mlPredictionClient;
    private final AssessmentResultRepository assessmentResultRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<String> getRequiredTop75Features() {
        return TOP_75_FEATURES;
    }

    @Override
    public List<String> validateFeatureCompleteness(Map<String, String> features) {
        if (features == null) {
            return new ArrayList<>(TOP_75_FEATURES);
        }
        List<String> missing = new ArrayList<>();
        for (String varName : TOP_75_FEATURES) {
            if (!features.containsKey(varName) || features.get(varName) == null) {
                missing.add(varName);
            }
        }
        return missing;
    }

    @Override
    public MlPredictionResponse predictArchetype(Map<String, String> features) {
        List<String> missing = validateFeatureCompleteness(features);
        if (!missing.isEmpty()) {
            log.warn("Cannot request ML prediction due to missing {} TOP_75 features: {}", missing.size(), missing);
            throw new IncompleteFeatureVectorException(missing);
        }

        MlPredictionRequest request = MlPredictionRequest.builder()
                .features(features)
                .build();

        return mlPredictionClient.predict(request);
    }

    @Override
    public com.financialliteracy.dto.ml.MlExplanationResponse explainArchetype(Map<String, String> features, String targetClass, Integer topK) {
        List<String> missing = validateFeatureCompleteness(features);
        if (!missing.isEmpty()) {
            log.warn("Cannot request TreeSHAP explanation due to missing {} TOP_75 features: {}", missing.size(), missing);
            throw new IncompleteFeatureVectorException(missing);
        }

        int k = (topK != null) ? topK : 5;
        if (k < 1 || k > 10) {
            throw new com.financialliteracy.exception.BadRequestException("top_k must be between 1 and 10, got " + k);
        }

        com.financialliteracy.dto.ml.MlExplanationRequest request = com.financialliteracy.dto.ml.MlExplanationRequest.builder()
                .features(features)
                .targetClass(targetClass)
                .topK(k)
                .build();

        return mlPredictionClient.explain(request);
    }

    @Override
    @Transactional
    public AssessmentResultResponse predictAndStoreAssessmentArchetype(UUID assessmentId, Map<String, String> features) {
        AssessmentResult result = assessmentResultRepository.findByAssessmentId(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment result not found for assessment id: " + assessmentId));

        MlPredictionResponse mlResponse = predictArchetype(features);

        result.setMlArchetype(mlResponse.getArchetype());
        result.setMlModelVersion(mlResponse.getModelVersion());
        result.setMlPredictedAt(Instant.now());

        try {
            String probasJson = objectMapper.writeValueAsString(mlResponse.getProbabilities());
            result.setMlProbabilitiesJson(probasJson);
        } catch (JsonProcessingException e) {
            log.warn("Could not serialize probabilities map to JSON string", e);
            result.setMlProbabilitiesJson("{}");
        }

        AssessmentResult saved = assessmentResultRepository.save(result);

        return AssessmentResultResponse.builder()
                .assessmentId(assessmentId)
                .totalQuestions(0) // Preserved or retrieved from existing result
                .correctAnswers((int) Math.round(saved.getTotalScore() != null ? saved.getTotalScore() : 0.0))
                .totalScore(saved.getTotalScore() != null ? saved.getTotalScore() : 0.0)
                .percentage(saved.getPercentage() != null ? saved.getPercentage() : 0.0)
                .proficiencyLevel(saved.getProficiencyLevel())
                .calculatedAt(saved.getCalculatedAt() != null ? saved.getCalculatedAt().toString() : Instant.now().toString())
                .mlArchetype(saved.getMlArchetype() != null ? saved.getMlArchetype().name() : null)
                .mlModelVersion(saved.getMlModelVersion())
                .mlPredictedAt(saved.getMlPredictedAt() != null ? saved.getMlPredictedAt().toString() : null)
                .mlProbabilities(mlResponse.getProbabilities())
                .build();
    }
}
