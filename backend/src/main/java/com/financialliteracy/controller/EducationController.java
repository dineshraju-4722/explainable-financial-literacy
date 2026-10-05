package com.financialliteracy.controller;

import com.financialliteracy.dto.education.EducationRecommendationResponse;
import com.financialliteracy.service.EducationRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/assessments")
@RequiredArgsConstructor
public class EducationController {

    private final EducationRecommendationService educationRecommendationService;

    @GetMapping("/{assessmentId}/education")
    public ResponseEntity<EducationRecommendationResponse> getRecommendations(@PathVariable UUID assessmentId) {
        EducationRecommendationResponse response = educationRecommendationService.generateRecommendations(assessmentId);
        return ResponseEntity.ok(response);
    }
}
