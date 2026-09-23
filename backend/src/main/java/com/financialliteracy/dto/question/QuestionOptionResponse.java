package com.financialliteracy.dto.question;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionOptionResponse {

    private UUID id;
    private String optionText;
    private Integer optionOrder;
}
