package com.financialliteracy.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryFeatureMetrics {
    private Integer questionCount;
    private Integer correctCount;
    /**
     * Category performance percentage.
     * Value is null if no questions were present/attempted for this category in the assessment.
     */
    private Double percentage;
}
