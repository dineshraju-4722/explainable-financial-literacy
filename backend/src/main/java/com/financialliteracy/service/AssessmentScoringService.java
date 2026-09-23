package com.financialliteracy.service;

import com.financialliteracy.dto.assessment.AssessmentResultResponse;
import com.financialliteracy.entity.Assessment;

public interface AssessmentScoringService {
    AssessmentResultResponse calculateAndStoreResult(Assessment assessment);
}
