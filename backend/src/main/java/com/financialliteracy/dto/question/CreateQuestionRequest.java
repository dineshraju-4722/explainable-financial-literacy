package com.financialliteracy.dto.question;

import com.financialliteracy.enums.DifficultyLevel;
import com.financialliteracy.enums.FinancialLiteracyCategory;
import com.financialliteracy.enums.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateQuestionRequest {

    @NotBlank(message = "Question text is required")
    private String questionText;

    @NotNull(message = "Category is required")
    private FinancialLiteracyCategory category;

    @NotNull(message = "Difficulty level is required")
    private DifficultyLevel difficulty;

    @NotNull(message = "Question type is required")
    private QuestionType questionType;

    @NotEmpty(message = "Options are required")
    @Valid
    private List<CreateQuestionOptionRequest> options;
}
