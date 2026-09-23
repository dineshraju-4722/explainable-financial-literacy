package com.financialliteracy.dto.analytics;

import com.financialliteracy.enums.DifficultyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DifficultyAnalyticsResponse {
    private DifficultyLevel difficulty;
    private int totalQuestions;
    private int correctAnswers;
    private int incorrectAnswers;
    private double percentage;
}
