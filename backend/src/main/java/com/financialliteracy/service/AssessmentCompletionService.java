package com.financialliteracy.service;

import com.financialliteracy.dto.assessment.AssessmentResultResponse;

import java.util.UUID;

public interface AssessmentCompletionService {
    AssessmentResultResponse completeAssessment(UUID assessmentId);
    AssessmentResultResponse getAssessmentResult(UUID assessmentId);
}
