package com.financialliteracy.dto.question;

import com.financialliteracy.enums.DifficultyLevel;
import com.financialliteracy.enums.FinancialLiteracyCategory;
import com.financialliteracy.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionResponse {

    private UUID id;
    private String questionText;
    private FinancialLiteracyCategory category;
    private DifficultyLevel difficulty;
    private QuestionType questionType;
    private Boolean active;
    private List<QuestionOptionResponse> options;
}
