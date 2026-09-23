package com.financialliteracy.dto.assessment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitAnswerRequest {

    @NotNull(message = "Question ID is required")
    private UUID questionId;

    @NotNull(message = "Selected Option ID is required")
    private UUID selectedOptionId;
}
