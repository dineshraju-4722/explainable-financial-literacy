package com.financialliteracy.controller;

import com.financialliteracy.dto.assessment.AssessmentResponseDTO;
import com.financialliteracy.dto.assessment.CreateAssessmentRequest;
import com.financialliteracy.dto.assessment.SubmitAnswerRequest;
import com.financialliteracy.dto.question.CreateQuestionOptionRequest;
import com.financialliteracy.dto.question.CreateQuestionRequest;
import com.financialliteracy.dto.question.QuestionResponse;
import com.financialliteracy.dto.user.CreateUserRequest;
import com.financialliteracy.dto.user.UserResponse;
import com.financialliteracy.entity.AssessmentResult;
import com.financialliteracy.enums.*;
import com.financialliteracy.repository.AssessmentResultRepository;
import com.financialliteracy.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EducationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AssessmentService assessmentService;

    @Autowired
    private AssessmentResponseService assessmentResponseService;

    @Autowired
    private AssessmentCompletionService assessmentCompletionService;

    @Autowired
    private AssessmentResultRepository assessmentResultRepository;

    private UserResponse user;
    private QuestionResponse question;

    @BeforeEach
    void setUp() {
        user = userService.createUser(CreateUserRequest.builder()
                .name("Taylor Swift")
                .email("taylor.swift@example.com")
                .age(30)
                .educationLevel("Graduate")
                .occupation("Musician")
                .build());

        question = questionService.createQuestion(CreateQuestionRequest.builder()
                .questionText("What is compound interest?")
                .category(FinancialLiteracyCategory.INTEREST)
                .difficulty(DifficultyLevel.BEGINNER)
                .questionType(QuestionType.MULTIPLE_CHOICE)
                .options(List.of(
                        CreateQuestionOptionRequest.builder().optionText("Interest on principal plus accumulated interest").optionOrder(1).isCorrect(true).build(),
                        CreateQuestionOptionRequest.builder().optionText("Simple fixed fee").optionOrder(2).isCorrect(false).build()
                ))
                .build());
    }

    @Test
    @DisplayName("GET /api/assessments/{id}/education returns 200 OK with recommendation response")
    void testGetRecommendationsEndpointSuccess() throws Exception {
        AssessmentResponseDTO assessment = assessmentService.createAssessment(CreateAssessmentRequest.builder()
                .userId(user.getId())
                .assessmentType(AssessmentType.INITIAL)
                .build());

        assessmentResponseService.submitAnswer(assessment.getId(), SubmitAnswerRequest.builder()
                .questionId(question.getId())
                .selectedOptionId(question.getOptions().get(0).getId())
                .build());

        assessmentCompletionService.completeAssessment(assessment.getId());

        AssessmentResult result = assessmentResultRepository.findByAssessmentId(assessment.getId()).orElseThrow();
        result.setMlArchetype(MlArchetype.COMPETENT);
        assessmentResultRepository.save(result);

        mockMvc.perform(get("/api/assessments/{assessmentId}/education", assessment.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assessmentId", is(assessment.getId().toString())))
                .andExpect(jsonPath("$.archetype", is("COMPETENT")))
                .andExpect(jsonPath("$.archetypeSummary", notNullValue()))
                .andExpect(jsonPath("$.generatedAt", notNullValue()))
                .andExpect(jsonPath("$.totalRecommendations", greaterThan(0)))
                .andExpect(jsonPath("$.recommendations", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.recommendations[0].recommendationId", notNullValue()))
                .andExpect(jsonPath("$.recommendations[0].topic", notNullValue()))
                .andExpect(jsonPath("$.recommendations[0].priority", notNullValue()))
                .andExpect(jsonPath("$.recommendations[0].recommendationType", notNullValue()))
                .andExpect(jsonPath("$.recommendations[0].title", notNullValue()))
                .andExpect(jsonPath("$.recommendations[0].rationale", notNullValue()))
                .andExpect(jsonPath("$.recommendations[0].learningObjective", notNullValue()))
                .andExpect(jsonPath("$.recommendations[0].estimatedDurationMinutes", greaterThan(0)));
    }

    @Test
    @DisplayName("GET /api/assessments/{id}/education returns 404 NOT FOUND for non-existent assessment")
    void testGetRecommendationsEndpointNotFound() throws Exception {
        UUID fakeId = UUID.randomUUID();

        mockMvc.perform(get("/api/assessments/{assessmentId}/education", fakeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Assessment not found")));
    }

    @Test
    @DisplayName("GET /api/assessments/{id}/education returns 400 BAD REQUEST for incomplete assessment")
    void testGetRecommendationsEndpointIncomplete() throws Exception {
        AssessmentResponseDTO assessment = assessmentService.createAssessment(CreateAssessmentRequest.builder()
                .userId(user.getId())
                .assessmentType(AssessmentType.INITIAL)
                .build());

        mockMvc.perform(get("/api/assessments/{assessmentId}/education", assessment.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Cannot generate recommendations for an incomplete assessment")));
    }
}
