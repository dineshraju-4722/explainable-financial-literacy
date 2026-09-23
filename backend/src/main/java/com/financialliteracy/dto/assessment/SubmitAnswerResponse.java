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
public class SubmitAnswerResponse {

    private UUID responseId;
    private UUID assessmentId;
    private UUID questionId;
    private UUID selectedOptionId;
    private String answeredAt;
    private boolean updatedExisting;
}
