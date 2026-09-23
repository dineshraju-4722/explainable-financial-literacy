package com.financialliteracy.service;

import com.financialliteracy.dto.analytics.AssessmentFeatureVector;
import com.financialliteracy.entity.Assessment;
import com.financialliteracy.entity.AssessmentResponse;
import com.financialliteracy.entity.AssessmentResult;

import java.util.List;

public interface FeatureEngineeringService {
    AssessmentFeatureVector extractFeatureVector(Assessment assessment, List<AssessmentResponse> responses, AssessmentResult result);
}
