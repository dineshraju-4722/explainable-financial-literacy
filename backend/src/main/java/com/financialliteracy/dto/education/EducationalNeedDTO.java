package com.financialliteracy.dto.education;

import com.financialliteracy.enums.FinancialLiteracyCategory;
import com.financialliteracy.enums.RecommendationPriority;
import com.financialliteracy.enums.RecommendationType;
import com.financialliteracy.enums.TopicPerformanceLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EducationalNeedDTO {
    private FinancialLiteracyCategory topic;
    private Double topicScorePercentage;
    private Integer totalQuestions;
    private Integer correctQuestions;
    private TopicPerformanceLevel performanceLevel;
    private RecommendationPriority priority;
    private RecommendationType recommendationType;
    private String rationale;
    private String learningObjective;
}
