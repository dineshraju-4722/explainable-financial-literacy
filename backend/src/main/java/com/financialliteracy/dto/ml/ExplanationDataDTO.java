package com.financialliteracy.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExplanationDataDTO {

    @JsonProperty("target_class")
    private String targetClass;

    @JsonProperty("top_k")
    private int topK;

    @JsonProperty("top_positive_contributors")
    private List<FeatureContributorDTO> topPositiveContributors;

    @JsonProperty("top_negative_contributors")
    private List<FeatureContributorDTO> topNegativeContributors;
}
