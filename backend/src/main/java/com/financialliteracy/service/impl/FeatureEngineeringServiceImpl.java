package com.financialliteracy.service.impl;

import com.financialliteracy.dto.analytics.AssessmentFeatureVector;
import com.financialliteracy.dto.analytics.CategoryFeatureMetrics;
import com.financialliteracy.dto.analytics.DifficultyFeatureMetrics;
import com.financialliteracy.entity.Assessment;
import com.financialliteracy.entity.AssessmentResponse;
import com.financialliteracy.entity.AssessmentResult;
import com.financialliteracy.enums.DifficultyLevel;
import com.financialliteracy.enums.FinancialLiteracyCategory;
import com.financialliteracy.service.FeatureEngineeringService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class FeatureEngineeringServiceImpl implements FeatureEngineeringService {

    @Override
    public AssessmentFeatureVector extractFeatureVector(Assessment assessment, List<AssessmentResponse> responses, AssessmentResult result) {
        int totalQuestions = responses.size();
        int correctAnswers = 0;

        for (AssessmentResponse resp : responses) {
            if (resp.getSelectedOption() != null && Boolean.TRUE.equals(resp.getSelectedOption().getIsCorrect())) {
                correctAnswers++;
            }
        }

        int incorrectAnswers = totalQuestions - correctAnswers;
        double overallPercentage = result != null ? roundToTwoDecimals(result.getPercentage()) : 
                (totalQuestions > 0 ? roundToTwoDecimals((double) correctAnswers / totalQuestions * 100.0) : 0.0);
        String proficiencyLevel = result != null ? result.getProficiencyLevel() : deriveProficiencyLevel(overallPercentage);

        // Category features
        Map<FinancialLiteracyCategory, CategoryFeatureMetrics> categoryFeatures = new EnumMap<>(FinancialLiteracyCategory.class);
        for (FinancialLiteracyCategory category : FinancialLiteracyCategory.values()) {
            int qCount = 0;
            int cCount = 0;

            for (AssessmentResponse resp : responses) {
                if (resp.getQuestion() != null && resp.getQuestion().getCategory() == category) {
                    qCount++;
                    if (resp.getSelectedOption() != null && Boolean.TRUE.equals(resp.getSelectedOption().getIsCorrect())) {
                        cCount++;
                    }
                }
            }

            Double percentage = (qCount > 0) ? roundToTwoDecimals((double) cCount / qCount * 100.0) : null;
            categoryFeatures.put(category, CategoryFeatureMetrics.builder()
                    .questionCount(qCount)
                    .correctCount(cCount)
                    .percentage(percentage)
                    .build());
        }

        // Difficulty features
        Map<DifficultyLevel, DifficultyFeatureMetrics> difficultyFeatures = new EnumMap<>(DifficultyLevel.class);
        for (DifficultyLevel difficulty : DifficultyLevel.values()) {
            int qCount = 0;
            int cCount = 0;

            for (AssessmentResponse resp : responses) {
                if (resp.getQuestion() != null && resp.getQuestion().getDifficulty() == difficulty) {
                    qCount++;
                    if (resp.getSelectedOption() != null && Boolean.TRUE.equals(resp.getSelectedOption().getIsCorrect())) {
                        cCount++;
                    }
                }
            }

            Double percentage = (qCount > 0) ? roundToTwoDecimals((double) cCount / qCount * 100.0) : null;
            difficultyFeatures.put(difficulty, DifficultyFeatureMetrics.builder()
                    .questionCount(qCount)
                    .correctCount(cCount)
                    .percentage(percentage)
                    .build());
        }

        return AssessmentFeatureVector.builder()
                .overallPercentage(overallPercentage)
                .totalQuestions(totalQuestions)
                .answeredQuestions(totalQuestions)
                .correctAnswers(correctAnswers)
                .incorrectAnswers(incorrectAnswers)
                .proficiencyLevel(proficiencyLevel)
                .assessmentType(assessment.getAssessmentType())
                .completionStatus(assessment.getStatus())
                .categoryFeatures(categoryFeatures)
                .difficultyFeatures(difficultyFeatures)
                .build();
    }

    private double roundToTwoDecimals(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private String deriveProficiencyLevel(double percentage) {
        if (percentage < 40.0) {
            return "BEGINNER";
        } else if (percentage < 70.0) {
            return "INTERMEDIATE";
        } else {
            return "ADVANCED";
        }
    }
}
