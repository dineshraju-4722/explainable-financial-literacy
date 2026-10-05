package com.financialliteracy.dto.education;

import com.financialliteracy.enums.MlArchetype;
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
public class EducationalNeedProfileDTO {
    private UUID assessmentId;
    private MlArchetype archetype;
    private String archetypeSummary;
    private List<EducationalNeedDTO> needs;
}
