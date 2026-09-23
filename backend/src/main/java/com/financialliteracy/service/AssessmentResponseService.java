package com.financialliteracy.service;

import com.financialliteracy.dto.assessment.SubmitAnswerRequest;
import com.financialliteracy.dto.assessment.SubmitAnswerResponse;

import java.util.UUID;

public interface AssessmentResponseService {
    SubmitAnswerResponse submitAnswer(UUID assessmentId, SubmitAnswerRequest request);
}
