package com.financialliteracy.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DifficultyFeatureMetrics {
    private Integer questionCount;
    private Integer correctCount;
    /**
     * Difficulty performance percentage.
     * Value is null if no questions were present/attempted for this difficulty level in the assessment.
     */
    private Double percentage;
}
