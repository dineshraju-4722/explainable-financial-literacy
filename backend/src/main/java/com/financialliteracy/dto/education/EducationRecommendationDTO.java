package com.financialliteracy.dto.education;

import com.financialliteracy.enums.DifficultyLevel;
import com.financialliteracy.enums.FinancialLiteracyCategory;
import com.financialliteracy.enums.RecommendationPriority;
import com.financialliteracy.enums.RecommendationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EducationRecommendationDTO {
    private UUID recommendationId;
    private UUID contentId;
    private FinancialLiteracyCategory topic;
    private RecommendationPriority priority;
    private RecommendationType recommendationType;
    private String title;
    private String shortDescription;
    private String rationale;
    private String learningObjective;
    private Integer estimatedDurationMinutes;
    private DifficultyLevel difficulty;
}
