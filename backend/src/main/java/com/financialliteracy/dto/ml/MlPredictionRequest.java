package com.financialliteracy.dto.ml;

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
public class MlPredictionRequest {

    @NotNull(message = "Features map must not be null")
    private Map<String, String> features;
}
