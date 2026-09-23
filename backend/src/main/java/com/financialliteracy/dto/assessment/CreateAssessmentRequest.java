package com.financialliteracy.dto.assessment;

import com.financialliteracy.enums.AssessmentType;
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
public class CreateAssessmentRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Assessment type is required")
    private AssessmentType assessmentType;
}
