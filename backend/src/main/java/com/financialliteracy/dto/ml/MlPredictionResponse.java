package com.financialliteracy.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.financialliteracy.enums.MlArchetype;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MlPredictionResponse {

    private MlArchetype archetype;

    private Map<String, Double> probabilities;

    @JsonProperty("model_version")
    private String modelVersion;

    private String timestamp;

    private Map<String, Object> metadata;
}
