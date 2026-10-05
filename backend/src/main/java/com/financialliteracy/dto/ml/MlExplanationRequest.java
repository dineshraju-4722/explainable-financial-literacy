package com.financialliteracy.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MlExplanationRequest {

    @NotNull(message = "Features map must not be null")
    private Map<String, String> features;

    @JsonProperty("target_class")
    private String targetClass;

    @JsonProperty("top_k")
    @Builder.Default
    private Integer topK = 5;
}
