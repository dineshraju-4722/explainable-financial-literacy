package com.financialliteracy.service.impl;

import com.financialliteracy.dto.assessment.SubmitAnswerRequest;
import com.financialliteracy.dto.assessment.SubmitAnswerResponse;
import com.financialliteracy.entity.Assessment;
import com.financialliteracy.entity.AssessmentResponse;
import com.financialliteracy.entity.Question;
import com.financialliteracy.entity.QuestionOption;
import com.financialliteracy.enums.AssessmentStatus;
import com.financialliteracy.exception.BadRequestException;
import com.financialliteracy.exception.IllegalStateTransitionException;
import com.financialliteracy.exception.ResourceNotFoundException;
import com.financialliteracy.repository.AssessmentRepository;
import com.financialliteracy.repository.AssessmentResponseRepository;
import com.financialliteracy.repository.QuestionOptionRepository;
import com.financialliteracy.repository.QuestionRepository;
import com.financialliteracy.service.AssessmentResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssessmentResponseServiceImpl implements AssessmentResponseService {

    private final AssessmentRepository assessmentRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final AssessmentResponseRepository assessmentResponseRepository;

    @Override
    @Transactional
    public SubmitAnswerResponse submitAnswer(UUID assessmentId, SubmitAnswerRequest request) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + assessmentId));

        if (assessment.getStatus() != AssessmentStatus.IN_PROGRESS) {
            throw new IllegalStateTransitionException("Cannot submit answers to an assessment with status: " + assessment.getStatus());
        }

        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + request.getQuestionId()));

        QuestionOption option = questionOptionRepository.findById(request.getSelectedOptionId())
                .orElseThrow(() -> new ResourceNotFoundException("Selected Option not found with id: " + request.getSelectedOptionId()));

        if (!option.getQuestion().getId().equals(question.getId())) {
            throw new BadRequestException("Selected option does not belong to the submitted question");
        }

        // Check if an answer for this (assessment_id, question_id) already exists
        List<AssessmentResponse> existingResponses = assessmentResponseRepository.findByAssessmentId(assessmentId);
        Optional<AssessmentResponse> existingResponseOpt = existingResponses.stream()
                .filter(r -> r.getQuestion().getId().equals(question.getId()))
                .findFirst();

        boolean updatedExisting = false;
        AssessmentResponse targetResponse;

        if (existingResponseOpt.isPresent()) {
            targetResponse = existingResponseOpt.get();
            targetResponse.setSelectedOption(option);
            targetResponse.setAnsweredAt(Instant.now());
            updatedExisting = true;
        } else {
            targetResponse = AssessmentResponse.builder()
                    .assessment(assessment)
                    .question(question)
                    .selectedOption(option)
                    .answeredAt(Instant.now())
                    .build();
        }

        AssessmentResponse saved = assessmentResponseRepository.save(targetResponse);

        return SubmitAnswerResponse.builder()
                .responseId(saved.getId())
                .assessmentId(assessmentId)
                .questionId(question.getId())
                .selectedOptionId(option.getId())
                .answeredAt(saved.getAnsweredAt().toString())
                .updatedExisting(updatedExisting)
                .build();
    }
}
