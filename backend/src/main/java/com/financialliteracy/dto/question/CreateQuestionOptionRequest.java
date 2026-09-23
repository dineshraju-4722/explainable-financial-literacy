package com.financialliteracy.dto.question;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateQuestionOptionRequest {

    @NotBlank(message = "Option text is required")
    private String optionText;

    @NotNull(message = "Option order is required")
    private Integer optionOrder;

    @NotNull(message = "Correctness flag is required")
    private Boolean isCorrect;
}
