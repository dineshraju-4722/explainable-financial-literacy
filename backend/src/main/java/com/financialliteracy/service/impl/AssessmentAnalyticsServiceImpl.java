package com.financialliteracy.service.impl;

import com.financialliteracy.dto.analytics.AssessmentAnalyticsResponse;
import com.financialliteracy.dto.analytics.AssessmentFeatureVector;
import com.financialliteracy.dto.analytics.CategoryAnalyticsResponse;
import com.financialliteracy.dto.analytics.DifficultyAnalyticsResponse;
import com.financialliteracy.entity.Assessment;
import com.financialliteracy.entity.AssessmentResponse;
import com.financialliteracy.entity.AssessmentResult;
import com.financialliteracy.enums.AssessmentStatus;
import com.financialliteracy.enums.DifficultyLevel;
import com.financialliteracy.enums.FinancialLiteracyCategory;
import com.financialliteracy.exception.IllegalStateTransitionException;
import com.financialliteracy.exception.ResourceNotFoundException;
import com.financialliteracy.repository.AssessmentRepository;
import com.financialliteracy.repository.AssessmentResponseRepository;
import com.financialliteracy.repository.AssessmentResultRepository;
import com.financialliteracy.service.AssessmentAnalyticsService;
import com.financialliteracy.service.FeatureEngineeringService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AssessmentAnalyticsServiceImpl implements AssessmentAnalyticsService {

    private final AssessmentRepository assessmentRepository;
    private final AssessmentResponseRepository assessmentResponseRepository;
    private final AssessmentResultRepository assessmentResultRepository;
    private final FeatureEngineeringService featureEngineeringService;

    @Override
    @Transactional(readOnly = true)
    public AssessmentAnalyticsResponse getAssessmentAnalytics(UUID assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + assessmentId));

        if (assessment.getStatus() != AssessmentStatus.COMPLETED) {
            throw new IllegalStateTransitionException("Cannot retrieve analytics for incomplete assessment. Current status is: " + assessment.getStatus());
        }

        AssessmentResult result = assessmentResultRepository.findByAssessmentId(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment result not found for completed assessment id: " + assessmentId));

        List<AssessmentResponse> responses = assessmentResponseRepository.findByAssessmentId(assessmentId);

        // Calculate Category Analytics for represented categories
        List<CategoryAnalyticsResponse> categoryAnalytics = new ArrayList<>();
        Map<FinancialLiteracyCategory, List<AssessmentResponse>> responsesByCategory = new LinkedHashMap<>();

        for (AssessmentResponse resp : responses) {
            if (resp.getQuestion() != null && resp.getQuestion().getCategory() != null) {
                responsesByCategory.computeIfAbsent(resp.getQuestion().getCategory(), k -> new ArrayList<>()).add(resp);
            }
        }

        for (Map.Entry<FinancialLiteracyCategory, List<AssessmentResponse>> entry : responsesByCategory.entrySet()) {
            FinancialLiteracyCategory cat = entry.getKey();
            List<AssessmentResponse> catResponses = entry.getValue();

            int totalQ = catResponses.size();
            int correctQ = 0;
            for (AssessmentResponse resp : catResponses) {
                if (resp.getSelectedOption() != null && Boolean.TRUE.equals(resp.getSelectedOption().getIsCorrect())) {
                    correctQ++;
                }
            }
            int incorrectQ = totalQ - correctQ;
            double score = (double) correctQ;
            double pct = roundToTwoDecimals((double) correctQ / totalQ * 100.0);

            categoryAnalytics.add(CategoryAnalyticsResponse.builder()
                    .category(cat)
                    .totalQuestions(totalQ)
                    .correctAnswers(correctQ)
                    .incorrectAnswers(incorrectQ)
                    .categoryScore(score)
                    .percentage(pct)
                    .build());
        }

        // Calculate Difficulty Analytics for represented difficulties
        List<DifficultyAnalyticsResponse> difficultyAnalytics = new ArrayList<>();
        Map<DifficultyLevel, List<AssessmentResponse>> responsesByDifficulty = new LinkedHashMap<>();

        for (AssessmentResponse resp : responses) {
            if (resp.getQuestion() != null && resp.getQuestion().getDifficulty() != null) {
                responsesByDifficulty.computeIfAbsent(resp.getQuestion().getDifficulty(), k -> new ArrayList<>()).add(resp);
            }
        }

        for (Map.Entry<DifficultyLevel, List<AssessmentResponse>> entry : responsesByDifficulty.entrySet()) {
            DifficultyLevel diff = entry.getKey();
            List<AssessmentResponse> diffResponses = entry.getValue();

            int totalQ = diffResponses.size();
            int correctQ = 0;
            for (AssessmentResponse resp : diffResponses) {
                if (resp.getSelectedOption() != null && Boolean.TRUE.equals(resp.getSelectedOption().getIsCorrect())) {
                    correctQ++;
                }
            }
            int incorrectQ = totalQ - correctQ;
            double pct = roundToTwoDecimals((double) correctQ / totalQ * 100.0);

            difficultyAnalytics.add(DifficultyAnalyticsResponse.builder()
                    .difficulty(diff)
                    .totalQuestions(totalQ)
                    .correctAnswers(correctQ)
                    .incorrectAnswers(incorrectQ)
                    .percentage(pct)
                    .build());
        }

        // Calculate Feature Vector
        AssessmentFeatureVector featureVector = featureEngineeringService.extractFeatureVector(assessment, responses, result);

        return AssessmentAnalyticsResponse.builder()
                .assessmentId(assessment.getId())
                .userId(assessment.getUser().getId())
                .assessmentType(assessment.getAssessmentType())
                .status(assessment.getStatus())
                .totalQuestions(responses.size())
                .answeredQuestions(responses.size())
                .correctAnswers(featureVector.getCorrectAnswers())
                .incorrectAnswers(featureVector.getIncorrectAnswers())
                .overallPercentage(roundToTwoDecimals(result.getPercentage()))
                .proficiencyLevel(result.getProficiencyLevel())
                .categoryAnalytics(categoryAnalytics)
                .difficultyAnalytics(difficultyAnalytics)
                .featureVector(featureVector)
                .build();
    }

    private double roundToTwoDecimals(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
