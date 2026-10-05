package com.financialliteracy.dto.assessment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentResultResponse {

    private UUID assessmentId;
    private int totalQuestions;
    private int correctAnswers;
    private double totalScore;
    private double percentage;
    private String proficiencyLevel;
    private String calculatedAt;
    private String mlArchetype;
    private String mlModelVersion;
    private String mlPredictedAt;
    private java.util.Map<String, Double> mlProbabilities;
}
