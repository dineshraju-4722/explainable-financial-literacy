package com.financialliteracy.ml;

import com.financialliteracy.dto.assessment.AssessmentResultResponse;
import com.financialliteracy.dto.ml.MlPredictionRequest;
import com.financialliteracy.dto.ml.MlPredictionResponse;
import com.financialliteracy.entity.Assessment;
import com.financialliteracy.entity.AssessmentResult;
import com.financialliteracy.enums.AssessmentStatus;
import com.financialliteracy.enums.AssessmentType;
import com.financialliteracy.enums.MlArchetype;
import com.financialliteracy.exception.IncompleteFeatureVectorException;
import com.financialliteracy.exception.MlServiceException;
import com.financialliteracy.exception.MlServiceUnavailableException;
import com.financialliteracy.repository.AssessmentRepository;
import com.financialliteracy.repository.AssessmentResultRepository;
import com.financialliteracy.service.MlPredictionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MlIntegrationTest {

    @Autowired
    private MlPredictionService mlPredictionService;

    @MockBean
    private MlPredictionClient mlPredictionClient;

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private AssessmentResultRepository assessmentResultRepository;

    private Map<String, String> validTop75Features;

    @BeforeEach
    void setUp() {
        validTop75Features = new HashMap<>();
        for (String varName : mlPredictionService.getRequiredTop75Features()) {
            validTop75Features.put(varName, "1");
        }
    }

    @Test
    @DisplayName("1. Successful ML prediction and response contract deserialization")
    void testSuccessfulPrediction() {
        Map<String, Double> probas = Map.of(
                "COMPETENT", 0.05,
                "OVERCONFIDENT", 0.80,
                "UNDERCONFIDENT", 0.05,
                "NAIVE", 0.10
        );

        MlPredictionResponse mockResponse = MlPredictionResponse.builder()
                .archetype(MlArchetype.OVERCONFIDENT)
                .probabilities(probas)
                .modelVersion("1.0.0")
                .timestamp("2026-10-04T20:00:00Z")
                .metadata(Map.of("accuracy", 0.5619, "macro_f1", 0.5205))
                .build();

        when(mlPredictionClient.predict(any(MlPredictionRequest.class))).thenReturn(mockResponse);

        MlPredictionResponse response = mlPredictionService.predictArchetype(validTop75Features);

        assertThat(response).isNotNull();
        assertThat(response.getArchetype()).isEqualTo(MlArchetype.OVERCONFIDENT);
        assertThat(response.getModelVersion()).isEqualTo("1.0.0");
        assertThat(response.getProbabilities()).containsEntry("OVERCONFIDENT", 0.80);
    }

    @Test
    @DisplayName("2. Missing TOP_75 features throws IncompleteFeatureVectorException with missing feature list")
    void testMissingRequiredFeaturesValidation() {
        Map<String, String> incompleteFeatures = new HashMap<>(validTop75Features);
        incompleteFeatures.remove("J20");
        incompleteFeatures.remove("B60");

        assertThatThrownBy(() -> mlPredictionService.predictArchetype(incompleteFeatures))
                .isInstanceOf(IncompleteFeatureVectorException.class)
                .hasMessageContaining("J20")
                .hasMessageContaining("B60");
    }

    @Test
    @DisplayName("3. ML Service Unavailable exception handling")
    void testMlServiceUnavailable() {
        when(mlPredictionClient.predict(any(MlPredictionRequest.class)))
                .thenThrow(new MlServiceUnavailableException("Service unreachable at http://localhost:8000/predict"));

        assertThatThrownBy(() -> mlPredictionService.predictArchetype(validTop75Features))
                .isInstanceOf(MlServiceUnavailableException.class)
                .hasMessageContaining("unreachable");
    }

    @Test
    @DisplayName("4. ML Service HTTP Error exception handling")
    void testMlServiceHttpError() {
        when(mlPredictionClient.predict(any(MlPredictionRequest.class)))
                .thenThrow(new MlServiceException("ML service returned HTTP 500"));

        assertThatThrownBy(() -> mlPredictionService.predictArchetype(validTop75Features))
                .isInstanceOf(MlServiceException.class)
                .hasMessageContaining("HTTP 500");
    }

    @Test
    @DisplayName("5. Successful TreeSHAP explanation deserialization and response mapping")
    void testSuccessfulExplanation() {
        com.financialliteracy.dto.ml.FeatureContributorDTO pos = com.financialliteracy.dto.ml.FeatureContributorDTO.builder()
                .feature("J1")
                .description("Difficulty Covering Monthly Expenses")
                .shapValue(0.124)
                .build();

        com.financialliteracy.dto.ml.FeatureContributorDTO neg = com.financialliteracy.dto.ml.FeatureContributorDTO.builder()
                .feature("M1_1")
                .description("Overall Financial Knowledge")
                .shapValue(-0.087)
                .build();

        com.financialliteracy.dto.ml.ExplanationDataDTO expData = com.financialliteracy.dto.ml.ExplanationDataDTO.builder()
                .targetClass("UNDERCONFIDENT")
                .topK(5)
                .topPositiveContributors(List.of(pos))
                .topNegativeContributors(List.of(neg))
                .build();

        com.financialliteracy.dto.ml.MlExplanationResponse mockResponse = com.financialliteracy.dto.ml.MlExplanationResponse.builder()
                .archetype(MlArchetype.UNDERCONFIDENT)
                .probabilities(Map.of("UNDERCONFIDENT", 0.70))
                .modelVersion("1.0.0")
                .timestamp("2026-10-04T20:00:00Z")
                .explanation(expData)
                .build();

        when(mlPredictionClient.explain(any(com.financialliteracy.dto.ml.MlExplanationRequest.class))).thenReturn(mockResponse);

        com.financialliteracy.dto.ml.MlExplanationResponse response = mlPredictionService.explainArchetype(validTop75Features, "UNDERCONFIDENT", 5);

        assertThat(response).isNotNull();
        assertThat(response.getArchetype()).isEqualTo(MlArchetype.UNDERCONFIDENT);
        assertThat(response.getExplanation().getTargetClass()).isEqualTo("UNDERCONFIDENT");
        assertThat(response.getExplanation().getTopPositiveContributors()).hasSize(1);
        assertThat(response.getExplanation().getTopPositiveContributors().get(0).getFeature()).isEqualTo("J1");
        assertThat(response.getExplanation().getTopNegativeContributors().get(0).getShapValue()).isEqualTo(-0.087);
    }

    @Test
    @DisplayName("6. Missing TOP_75 features throws IncompleteFeatureVectorException on explanation request")
    void testIncompleteFeatureVectorExplanation() {
        Map<String, String> incompleteFeatures = new HashMap<>(validTop75Features);
        incompleteFeatures.remove("M1_1");

        assertThatThrownBy(() -> mlPredictionService.explainArchetype(incompleteFeatures, "COMPETENT", 5))
                .isInstanceOf(IncompleteFeatureVectorException.class)
                .hasMessageContaining("M1_1");
    }
}
