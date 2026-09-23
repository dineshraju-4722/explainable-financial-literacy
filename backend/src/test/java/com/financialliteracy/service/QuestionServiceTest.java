package com.financialliteracy.service;

import com.financialliteracy.dto.question.CreateQuestionOptionRequest;
import com.financialliteracy.dto.question.CreateQuestionRequest;
import com.financialliteracy.dto.question.QuestionResponse;
import com.financialliteracy.enums.DifficultyLevel;
import com.financialliteracy.enums.FinancialLiteracyCategory;
import com.financialliteracy.enums.QuestionType;
import com.financialliteracy.exception.BadRequestException;
import com.financialliteracy.repository.QuestionOptionRepository;
import com.financialliteracy.repository.QuestionRepository;
import com.financialliteracy.service.impl.QuestionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionOptionRepository questionOptionRepository;

    private QuestionService questionService;

    @BeforeEach
    void setUp() {
        questionService = new QuestionServiceImpl(questionRepository, questionOptionRepository);
    }

    @Test
    @DisplayName("Should reject question creation when zero options are provided")
    void testCreateQuestionEmptyOptions() {
        CreateQuestionRequest request = CreateQuestionRequest.builder()
                .questionText("Sample Question?")
                .category(FinancialLiteracyCategory.BUDGETING)
                .difficulty(DifficultyLevel.BEGINNER)
                .questionType(QuestionType.MULTIPLE_CHOICE)
                .options(List.of())
                .build();

        assertThatThrownBy(() -> questionService.createQuestion(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Question options list cannot be empty");
    }

    @Test
    @DisplayName("Should reject TRUE_FALSE question when options count is not 2")
    void testCreateTrueFalseInvalidOptionsCount() {
        CreateQuestionRequest request = CreateQuestionRequest.builder()
                .questionText("Inflation decreases purchasing power?")
                .category(FinancialLiteracyCategory.INFLATION)
                .difficulty(DifficultyLevel.BEGINNER)
                .questionType(QuestionType.TRUE_FALSE)
                .options(List.of(
                        CreateQuestionOptionRequest.builder().optionText("True").optionOrder(1).isCorrect(true).build()
                ))
                .build();

        assertThatThrownBy(() -> questionService.createQuestion(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("TRUE_FALSE questions must contain exactly 2 options");
    }

    @Test
    @DisplayName("Should reject question when zero or multiple correct options are flagged")
    void testCreateQuestionInvalidCorrectCount() {
        CreateQuestionRequest request = CreateQuestionRequest.builder()
                .questionText("Select budgeting method:")
                .category(FinancialLiteracyCategory.BUDGETING)
                .difficulty(DifficultyLevel.INTERMEDIATE)
                .questionType(QuestionType.MULTIPLE_CHOICE)
                .options(List.of(
                        CreateQuestionOptionRequest.builder().optionText("50/30/20 Rule").optionOrder(1).isCorrect(true).build(),
                        CreateQuestionOptionRequest.builder().optionText("Zero-based Budgeting").optionOrder(2).isCorrect(true).build()
                ))
                .build();

        assertThatThrownBy(() -> questionService.createQuestion(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Question must contain exactly ONE correct option");
    }
}
