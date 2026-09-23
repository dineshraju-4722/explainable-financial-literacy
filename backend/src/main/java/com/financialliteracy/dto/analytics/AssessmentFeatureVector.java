package com.financialliteracy.dto.analytics;

import com.financialliteracy.enums.AssessmentStatus;
import com.financialliteracy.enums.AssessmentType;
import com.financialliteracy.enums.DifficultyLevel;
import com.financialliteracy.enums.FinancialLiteracyCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentFeatureVector {
    private Double overallPercentage;
    private Integer totalQuestions;
    private Integer answeredQuestions;
    private Integer correctAnswers;
    private Integer incorrectAnswers;
    private String proficiencyLevel;
    private AssessmentType assessmentType;
    private AssessmentStatus completionStatus;

    private Map<FinancialLiteracyCategory, CategoryFeatureMetrics> categoryFeatures;
    private Map<DifficultyLevel, DifficultyFeatureMetrics> difficultyFeatures;
}
