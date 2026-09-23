package com.financialliteracy.dto.analytics;

import com.financialliteracy.enums.AssessmentStatus;
import com.financialliteracy.enums.AssessmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentAnalyticsResponse {
    private UUID assessmentId;
    private UUID userId;
    private AssessmentType assessmentType;
    private AssessmentStatus status;
    private int totalQuestions;
    private int answeredQuestions;
    private int correctAnswers;
    private int incorrectAnswers;
    private double overallPercentage;
    private String proficiencyLevel;

    private List<CategoryAnalyticsResponse> categoryAnalytics;
    private List<DifficultyAnalyticsResponse> difficultyAnalytics;

    private AssessmentFeatureVector featureVector;
}
