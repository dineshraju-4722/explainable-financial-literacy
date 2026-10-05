package com.financialliteracy.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureContributorDTO {

    private String feature;

    private String description;

    @JsonProperty("shap_value")
    private Double shapValue;
}
