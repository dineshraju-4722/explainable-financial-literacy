package com.financialliteracy.service.impl;

import com.financialliteracy.dto.assessment.AssessmentResultResponse;
import com.financialliteracy.entity.Assessment;
import com.financialliteracy.entity.AssessmentResponse;
import com.financialliteracy.entity.AssessmentResult;
import com.financialliteracy.enums.AssessmentStatus;
import com.financialliteracy.exception.IllegalStateTransitionException;
import com.financialliteracy.exception.ResourceNotFoundException;
import com.financialliteracy.repository.AssessmentRepository;
import com.financialliteracy.repository.AssessmentResponseRepository;
import com.financialliteracy.repository.AssessmentResultRepository;
import com.financialliteracy.service.AssessmentCompletionService;
import com.financialliteracy.service.AssessmentScoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssessmentCompletionServiceImpl implements AssessmentCompletionService {

    private final AssessmentRepository assessmentRepository;
    private final AssessmentResponseRepository assessmentResponseRepository;
    private final AssessmentResultRepository assessmentResultRepository;
    private final AssessmentScoringService assessmentScoringService;

    @Override
    @Transactional
    public AssessmentResultResponse completeAssessment(UUID assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + assessmentId));

        if (assessment.getStatus() != AssessmentStatus.IN_PROGRESS) {
            throw new IllegalStateTransitionException("Cannot complete assessment. Current status is already: " + assessment.getStatus());
        }

        // Calculate score and store AssessmentResult entity
        AssessmentResultResponse resultResponse = assessmentScoringService.calculateAndStoreResult(assessment);

        // Transition status to COMPLETED
        assessment.setStatus(AssessmentStatus.COMPLETED);
        assessment.setCompletedAt(Instant.now());
        assessmentRepository.save(assessment);

        return resultResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public AssessmentResultResponse getAssessmentResult(UUID assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + assessmentId));

        AssessmentResult result = assessmentResultRepository.findByAssessmentId(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment result not found for assessment id: " + assessmentId));

        List<AssessmentResponse> responses = assessmentResponseRepository.findByAssessmentId(assessmentId);
        int totalQuestions = responses.size();
        int correctAnswers = (int) Math.round(result.getTotalScore());

        return AssessmentResultResponse.builder()
                .assessmentId(assessment.getId())
                .totalQuestions(totalQuestions)
                .correctAnswers(correctAnswers)
                .totalScore(result.getTotalScore())
                .percentage(result.getPercentage())
                .proficiencyLevel(result.getProficiencyLevel())
                .calculatedAt(result.getCalculatedAt().toString())
                .build();
    }
}
