package com.financialliteracy.dto.question;

import com.financialliteracy.enums.DifficultyLevel;
import com.financialliteracy.enums.FinancialLiteracyCategory;
import com.financialliteracy.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateQuestionRequest {

    private String questionText;
    private FinancialLiteracyCategory category;
    private DifficultyLevel difficulty;
    private QuestionType questionType;
    private Boolean active;
}
