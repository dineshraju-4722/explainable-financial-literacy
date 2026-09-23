package com.financialliteracy.service.impl;

import com.financialliteracy.dto.assessment.AssessmentResponseDTO;
import com.financialliteracy.dto.assessment.CreateAssessmentRequest;
import com.financialliteracy.dto.question.QuestionResponse;
import com.financialliteracy.entity.Assessment;
import com.financialliteracy.entity.User;
import com.financialliteracy.enums.AssessmentStatus;
import com.financialliteracy.exception.ResourceNotFoundException;
import com.financialliteracy.repository.AssessmentRepository;
import com.financialliteracy.repository.UserRepository;
import com.financialliteracy.service.AssessmentService;
import com.financialliteracy.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssessmentServiceImpl implements AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final UserRepository userRepository;
    private final QuestionService questionService;

    @Override
    @Transactional
    public AssessmentResponseDTO createAssessment(CreateAssessmentRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        Assessment assessment = Assessment.builder()
                .user(user)
                .assessmentType(request.getAssessmentType())
                .status(AssessmentStatus.IN_PROGRESS)
                .build();

        Assessment savedAssessment = assessmentRepository.save(assessment);
        return mapToDTO(savedAssessment);
    }

    @Override
    @Transactional(readOnly = true)
    public AssessmentResponseDTO getAssessmentById(UUID id) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + id));
        return mapToDTO(assessment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponse> getAssessmentQuestions(UUID assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + assessmentId));

        // Returns active questions with safe option DTOs (no isCorrect exposure)
        return questionService.getQuestions(null, null, null, true);
    }

    private AssessmentResponseDTO mapToDTO(Assessment assessment) {
        return AssessmentResponseDTO.builder()
                .id(assessment.getId())
                .userId(assessment.getUser().getId())
                .assessmentType(assessment.getAssessmentType())
                .status(assessment.getStatus())
                .startedAt(assessment.getStartedAt() != null ? assessment.getStartedAt().toString() : null)
                .completedAt(assessment.getCompletedAt() != null ? assessment.getCompletedAt().toString() : null)
                .build();
    }
}
