package com.financialliteracy.service;

import com.financialliteracy.dto.education.EducationRecommendationResponse;

import java.util.UUID;

public interface EducationRecommendationService {
    EducationRecommendationResponse generateRecommendations(UUID assessmentId);
}
