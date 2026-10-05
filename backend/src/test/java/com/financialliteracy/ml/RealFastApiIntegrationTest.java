package com.financialliteracy.ml;

import com.financialliteracy.dto.ml.MlPredictionRequest;
import com.financialliteracy.dto.ml.MlPredictionResponse;
import com.financialliteracy.enums.MlArchetype;
import com.financialliteracy.service.impl.MlPredictionServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class RealFastApiIntegrationTest {

    @Autowired
    private MlPredictionClient mlPredictionClient;

    @Test
    @DisplayName("Real Integration Test: Spring Boot -> FastAPI ML Service -> TOP_75 LightGBM")
    void testRealSpringToFastApiCommunication() {
        Map<String, String> features = new HashMap<>();
        for (String varName : MlPredictionServiceImpl.TOP_75_FEATURES) {
            features.put(varName, "1");
        }
        // Customize a few key financial indicators
        features.put("M1_1", "7"); // High self-assessment
        features.put("J20", "9");  // High financial satisfaction
        features.put("J1", "1");   // Low expense difficulty

        MlPredictionRequest request = MlPredictionRequest.builder()
                .features(features)
                .build();

        // Perform real HTTP POST to live FastAPI service at http://localhost:8000/predict
        MlPredictionResponse response = mlPredictionClient.predict(request);

        assertThat(response).isNotNull();
        assertThat(response.getArchetype()).isIn(
                MlArchetype.COMPETENT,
                MlArchetype.OVERCONFIDENT,
                MlArchetype.UNDERCONFIDENT,
                MlArchetype.NAIVE
        );
        assertThat(response.getModelVersion()).isEqualTo("1.0.0");
        assertThat(response.getProbabilities()).hasSize(4);
        assertThat(response.getProbabilities()).containsKeys("COMPETENT", "OVERCONFIDENT", "UNDERCONFIDENT", "NAIVE");

        System.out.println("============================================================");
        System.out.println("REAL SPRING BOOT -> FASTAPI INTEGRATION SUCCESSFUL!");
        System.out.println("Predicted Archetype: " + response.getArchetype());
        System.out.println("Probabilities Breakdown: " + response.getProbabilities());
        System.out.println("Model Version: " + response.getModelVersion());
        System.out.println("============================================================");
    }

    @Test
    @DisplayName("Real Integration Test: Spring Boot -> FastAPI TreeSHAP Explanation & Consistency")
    void testRealSpringToFastApiTreeShapExplanation() {
        Map<String, String> features = new HashMap<>();
        for (String varName : MlPredictionServiceImpl.TOP_75_FEATURES) {
            features.put(varName, "1");
        }
        features.put("M1_1", "2"); // Low self-assessed knowledge
        features.put("J1", "7");   // High expense difficulty

        // 1. Get Prediction
        MlPredictionRequest predReq = MlPredictionRequest.builder().features(features).build();
        MlPredictionResponse predResp = mlPredictionClient.predict(predReq);

        // 2. Get Explanation
        com.financialliteracy.dto.ml.MlExplanationRequest expReq = com.financialliteracy.dto.ml.MlExplanationRequest.builder()
                .features(features)
                .topK(5)
                .build();

        com.financialliteracy.dto.ml.MlExplanationResponse expResp = mlPredictionClient.explain(expReq);

        // 3. Consistency Assertions
        assertThat(expResp.getArchetype()).isEqualTo(predResp.getArchetype());
        assertThat(expResp.getModelVersion()).isEqualTo(predResp.getModelVersion());

        for (String c : predResp.getProbabilities().keySet()) {
            double p1 = predResp.getProbabilities().get(c);
            double p2 = expResp.getProbabilities().get(c);
            assertThat(Math.abs(p1 - p2)).isLessThan(0.0001);
        }

        // 4. Explanation Content Assertions
        com.financialliteracy.dto.ml.ExplanationDataDTO explanation = expResp.getExplanation();
        assertThat(explanation).isNotNull();
        assertThat(explanation.getTargetClass()).isEqualTo(predResp.getArchetype().name());
        assertThat(explanation.getTopK()).isEqualTo(5);
        assertThat(explanation.getTopPositiveContributors()).isNotNull();
        assertThat(explanation.getTopNegativeContributors()).isNotNull();

        System.out.println("============================================================");
        System.out.println("REAL SPRING BOOT -> FASTAPI TREESHAP EXPLANATION SUCCESSFUL!");
        System.out.println("Target Class: " + explanation.getTargetClass());
        System.out.println("Top Positive Drivers: " + explanation.getTopPositiveContributors());
        System.out.println("Top Negative Drivers: " + explanation.getTopNegativeContributors());
        System.out.println("============================================================");
    }
}
