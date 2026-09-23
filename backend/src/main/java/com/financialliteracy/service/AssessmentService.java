package com.financialliteracy.service;

import com.financialliteracy.dto.assessment.AssessmentResponseDTO;
import com.financialliteracy.dto.assessment.CreateAssessmentRequest;
import com.financialliteracy.dto.question.QuestionResponse;

import java.util.List;
import java.util.UUID;

public interface AssessmentService {
    AssessmentResponseDTO createAssessment(CreateAssessmentRequest request);
    AssessmentResponseDTO getAssessmentById(UUID id);
    List<QuestionResponse> getAssessmentQuestions(UUID assessmentId);
}
