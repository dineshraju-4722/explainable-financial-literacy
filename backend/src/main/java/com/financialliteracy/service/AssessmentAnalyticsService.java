package com.financialliteracy.service;

import com.financialliteracy.dto.analytics.AssessmentAnalyticsResponse;

import java.util.UUID;

public interface AssessmentAnalyticsService {
    AssessmentAnalyticsResponse getAssessmentAnalytics(UUID assessmentId);
}
