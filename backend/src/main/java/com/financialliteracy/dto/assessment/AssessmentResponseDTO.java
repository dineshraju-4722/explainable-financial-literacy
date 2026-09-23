package com.financialliteracy.dto.assessment;

import com.financialliteracy.enums.AssessmentStatus;
import com.financialliteracy.enums.AssessmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentResponseDTO {

    private UUID id;
    private UUID userId;
    private AssessmentType assessmentType;
    private AssessmentStatus status;
    private String startedAt;
    private String completedAt;
}
