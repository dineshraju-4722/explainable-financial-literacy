package com.financialliteracy.dto.analytics;

import com.financialliteracy.enums.FinancialLiteracyCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryAnalyticsResponse {
    private FinancialLiteracyCategory category;
    private int totalQuestions;
    private int correctAnswers;
    private int incorrectAnswers;
    private double categoryScore;
    private double percentage;
}
