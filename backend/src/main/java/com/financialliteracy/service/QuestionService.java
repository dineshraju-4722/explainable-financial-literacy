package com.financialliteracy.service;

import com.financialliteracy.dto.question.CreateQuestionRequest;
import com.financialliteracy.dto.question.QuestionResponse;
import com.financialliteracy.dto.question.UpdateQuestionRequest;
import com.financialliteracy.enums.DifficultyLevel;
import com.financialliteracy.enums.FinancialLiteracyCategory;
import com.financialliteracy.enums.QuestionType;

import java.util.List;
import java.util.UUID;

public interface QuestionService {
    QuestionResponse createQuestion(CreateQuestionRequest request);
    QuestionResponse getQuestionById(UUID id);
    List<QuestionResponse> getQuestions(FinancialLiteracyCategory category, DifficultyLevel difficulty, QuestionType questionType, Boolean active);
    QuestionResponse updateQuestion(UUID id, UpdateQuestionRequest request);
    QuestionResponse setQuestionStatus(UUID id, boolean active);
}
