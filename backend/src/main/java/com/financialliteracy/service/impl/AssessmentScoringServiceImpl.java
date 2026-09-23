package com.financialliteracy.service.impl;

import com.financialliteracy.dto.assessment.AssessmentResultResponse;
import com.financialliteracy.entity.Assessment;
import com.financialliteracy.entity.AssessmentResponse;
import com.financialliteracy.entity.AssessmentResult;
import com.financialliteracy.exception.BadRequestException;
import com.financialliteracy.repository.AssessmentResponseRepository;
import com.financialliteracy.repository.AssessmentResultRepository;
import com.financialliteracy.service.AssessmentScoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AssessmentScoringServiceImpl implements AssessmentScoringService {

    private final AssessmentResponseRepository assessmentResponseRepository;
    private final AssessmentResultRepository assessmentResultRepository;

    @Override
    @Transactional
    public AssessmentResultResponse calculateAndStoreResult(Assessment assessment) {
        List<AssessmentResponse> responses = assessmentResponseRepository.findByAssessmentId(assessment.getId());

        if (responses.isEmpty()) {
            throw new BadRequestException("Cannot calculate result for an assessment with zero responses");
        }

        int totalQuestions = responses.size();
        int correctAnswers = 0;

        for (AssessmentResponse resp : responses) {
            if (resp.getSelectedOption() != null && Boolean.TRUE.equals(resp.getSelectedOption().getIsCorrect())) {
                correctAnswers++;
            }
        }

        double totalScore = (double) correctAnswers;
        double percentage = (double) correctAnswers / totalQuestions * 100.0;
        String proficiencyLevel = deriveProficiencyLevel(percentage);

        // Retrieve existing or create new AssessmentResult entity
        Optional<AssessmentResult> existingResultOpt = assessmentResultRepository.findByAssessmentId(assessment.getId());
        AssessmentResult result;

        if (existingResultOpt.isPresent()) {
            result = existingResultOpt.get();
            result.setTotalScore(totalScore);
            result.setPercentage(percentage);
            result.setProficiencyLevel(proficiencyLevel);
            result.setCalculatedAt(Instant.now());
        } else {
            result = AssessmentResult.builder()
                    .assessment(assessment)
                    .totalScore(totalScore)
                    .percentage(percentage)
                    .proficiencyLevel(proficiencyLevel)
                    .calculatedAt(Instant.now())
                    .build();
        }

        AssessmentResult savedResult = assessmentResultRepository.save(result);

        return AssessmentResultResponse.builder()
                .assessmentId(assessment.getId())
                .totalQuestions(totalQuestions)
                .correctAnswers(correctAnswers)
                .totalScore(savedResult.getTotalScore())
                .percentage(savedResult.getPercentage())
                .proficiencyLevel(savedResult.getProficiencyLevel())
                .calculatedAt(savedResult.getCalculatedAt().toString())
                .build();
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
