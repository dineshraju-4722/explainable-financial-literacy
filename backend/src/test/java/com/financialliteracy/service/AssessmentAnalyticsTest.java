package com.financialliteracy.service;

import com.financialliteracy.dto.analytics.*;
import com.financialliteracy.dto.assessment.*;
import com.financialliteracy.dto.question.*;
import com.financialliteracy.dto.user.CreateUserRequest;
import com.financialliteracy.dto.user.UserResponse;
import com.financialliteracy.enums.*;
import com.financialliteracy.exception.IllegalStateTransitionException;
import com.financialliteracy.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AssessmentAnalyticsTest {

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
    private AssessmentAnalyticsService assessmentAnalyticsService;

    private UserResponse testUser;
    private QuestionResponse qBudgetingBeginner;
    private QuestionResponse qSavingIntermediate;
    private QuestionResponse qInvestingAdvanced;

    @BeforeEach
    void setUp() {
        // Create User
        testUser = userService.createUser(CreateUserRequest.builder()
                .name("Jane Doe")
                .email("jane.doe@university.edu")
                .age(25)
                .educationLevel("Graduate")
                .occupation("Analyst")
                .build());

        // Create Q1: BUDGETING / BEGINNER
        qBudgetingBeginner = questionService.createQuestion(CreateQuestionRequest.builder()
                .questionText("What is a recommended emergency fund size?")
                .category(FinancialLiteracyCategory.BUDGETING)
                .difficulty(DifficultyLevel.BEGINNER)
                .questionType(QuestionType.MULTIPLE_CHOICE)
                .options(List.of(
                        CreateQuestionOptionRequest.builder().optionText("3-6 months expenses").optionOrder(1).isCorrect(true).build(),
                        CreateQuestionOptionRequest.builder().optionText("1 week income").optionOrder(2).isCorrect(false).build()
                ))
                .build());

        // Create Q2: SAVING / INTERMEDIATE
        qSavingIntermediate = questionService.createQuestion(CreateQuestionRequest.builder()
                .questionText("High-yield savings accounts typically yield more than traditional accounts.")
                .category(FinancialLiteracyCategory.SAVING)
                .difficulty(DifficultyLevel.INTERMEDIATE)
                .questionType(QuestionType.TRUE_FALSE)
                .options(List.of(
                        CreateQuestionOptionRequest.builder().optionText("True").optionOrder(1).isCorrect(true).build(),
                        CreateQuestionOptionRequest.builder().optionText("False").optionOrder(2).isCorrect(false).build()
                ))
                .build());

        // Create Q3: INVESTING / ADVANCED
        qInvestingAdvanced = questionService.createQuestion(CreateQuestionRequest.builder()
                .questionText("Options contracts give the holder an obligation to buy stocks.")
                .category(FinancialLiteracyCategory.INVESTING)
                .difficulty(DifficultyLevel.ADVANCED)
                .questionType(QuestionType.TRUE_FALSE)
                .options(List.of(
                        CreateQuestionOptionRequest.builder().optionText("True").optionOrder(1).isCorrect(false).build(),
                        CreateQuestionOptionRequest.builder().optionText("False").optionOrder(2).isCorrect(true).build()
                ))
                .build());
    }

    @Test
    @DisplayName("Should reject analytics for IN_PROGRESS assessment")
    void testAnalyticsForInProgressAssessmentShouldFail() {
        AssessmentResponseDTO assessment = assessmentService.createAssessment(CreateAssessmentRequest.builder()
                .userId(testUser.getId())
                .assessmentType(AssessmentType.INITIAL)
                .build());

        assertThatThrownBy(() -> assessmentAnalyticsService.getAssessmentAnalytics(assessment.getId()))
                .isInstanceOf(IllegalStateTransitionException.class)
                .hasMessageContaining("Cannot retrieve analytics for incomplete assessment. Current status is: IN_PROGRESS");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for non-existent assessment")
    void testAnalyticsForNonExistentAssessmentShouldFail() {
        UUID fakeId = UUID.randomUUID();
        assertThatThrownBy(() -> assessmentAnalyticsService.getAssessmentAnalytics(fakeId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Assessment not found with id: " + fakeId);
    }

    @Test
    @DisplayName("Should calculate analytics correctly for 100% score")
    void testAnalyticsForPerfectScore() {
        AssessmentResponseDTO assessment = assessmentService.createAssessment(CreateAssessmentRequest.builder()
                .userId(testUser.getId())
                .assessmentType(AssessmentType.INITIAL)
                .build());

        // Submit all correct answers
        submitCorrectAnswer(assessment.getId(), qBudgetingBeginner);
        submitCorrectAnswer(assessment.getId(), qSavingIntermediate);
        submitCorrectAnswer(assessment.getId(), qInvestingAdvanced);

        assessmentCompletionService.completeAssessment(assessment.getId());

        AssessmentAnalyticsResponse analytics = assessmentAnalyticsService.getAssessmentAnalytics(assessment.getId());

        assertThat(analytics.getAssessmentId()).isEqualTo(assessment.getId());
        assertThat(analytics.getUserId()).isEqualTo(testUser.getId());
        assertThat(analytics.getAssessmentType()).isEqualTo(AssessmentType.INITIAL);
        assertThat(analytics.getStatus()).isEqualTo(AssessmentStatus.COMPLETED);
        assertThat(analytics.getTotalQuestions()).isEqualTo(3);
        assertThat(analytics.getAnsweredQuestions()).isEqualTo(3);
        assertThat(analytics.getCorrectAnswers()).isEqualTo(3);
        assertThat(analytics.getIncorrectAnswers()).isEqualTo(0);
        assertThat(analytics.getOverallPercentage()).isEqualTo(100.0);
        assertThat(analytics.getProficiencyLevel()).isEqualTo("ADVANCED");

        // Verify Category Analytics
        assertThat(analytics.getCategoryAnalytics()).hasSize(3);
        for (CategoryAnalyticsResponse catResp : analytics.getCategoryAnalytics()) {
            assertThat(catResp.getTotalQuestions()).isEqualTo(1);
            assertThat(catResp.getCorrectAnswers()).isEqualTo(1);
            assertThat(catResp.getIncorrectAnswers()).isEqualTo(0);
            assertThat(catResp.getPercentage()).isEqualTo(100.0);
        }

        // Verify Difficulty Analytics
        assertThat(analytics.getDifficultyAnalytics()).hasSize(3);
        for (DifficultyAnalyticsResponse diffResp : analytics.getDifficultyAnalytics()) {
            assertThat(diffResp.getTotalQuestions()).isEqualTo(1);
            assertThat(diffResp.getCorrectAnswers()).isEqualTo(1);
            assertThat(diffResp.getIncorrectAnswers()).isEqualTo(0);
            assertThat(diffResp.getPercentage()).isEqualTo(100.0);
        }

        // Verify Feature Vector
        AssessmentFeatureVector fv = analytics.getFeatureVector();
        assertThat(fv.getOverallPercentage()).isEqualTo(100.0);
        assertThat(fv.getCategoryFeatures().get(FinancialLiteracyCategory.BUDGETING).getPercentage()).isEqualTo(100.0);
        assertThat(fv.getCategoryFeatures().get(FinancialLiteracyCategory.SAVING).getPercentage()).isEqualTo(100.0);
        assertThat(fv.getCategoryFeatures().get(FinancialLiteracyCategory.INVESTING).getPercentage()).isEqualTo(100.0);
        // Verify missing category has null percentage
        assertThat(fv.getCategoryFeatures().get(FinancialLiteracyCategory.CREDIT).getPercentage()).isNull();
        assertThat(fv.getCategoryFeatures().get(FinancialLiteracyCategory.CREDIT).getQuestionCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should calculate analytics correctly for 0% score")
    void testAnalyticsForZeroScore() {
        AssessmentResponseDTO assessment = assessmentService.createAssessment(CreateAssessmentRequest.builder()
                .userId(testUser.getId())
                .assessmentType(AssessmentType.INITIAL)
                .build());

        // Submit all incorrect answers
        submitIncorrectAnswer(assessment.getId(), qBudgetingBeginner);
        submitIncorrectAnswer(assessment.getId(), qSavingIntermediate);

        assessmentCompletionService.completeAssessment(assessment.getId());

        AssessmentAnalyticsResponse analytics = assessmentAnalyticsService.getAssessmentAnalytics(assessment.getId());

        assertThat(analytics.getTotalQuestions()).isEqualTo(2);
        assertThat(analytics.getCorrectAnswers()).isEqualTo(0);
        assertThat(analytics.getIncorrectAnswers()).isEqualTo(2);
        assertThat(analytics.getOverallPercentage()).isEqualTo(0.0);
        assertThat(analytics.getProficiencyLevel()).isEqualTo("BEGINNER");

        AssessmentFeatureVector fv = analytics.getFeatureVector();
        // Attemped category with 0 correct must return 0.0 percentage (not null)
        assertThat(fv.getCategoryFeatures().get(FinancialLiteracyCategory.BUDGETING).getPercentage()).isEqualTo(0.0);
        assertThat(fv.getCategoryFeatures().get(FinancialLiteracyCategory.BUDGETING).getQuestionCount()).isEqualTo(1);
        // Unattempted category must return null percentage
        assertThat(fv.getCategoryFeatures().get(FinancialLiteracyCategory.TAXATION).getPercentage()).isNull();
    }

    @Test
    @DisplayName("Should calculate analytics correctly for mixed score and verify missing difficulty handling")
    void testAnalyticsForMixedScoreAndDifficultyHandling() {
        AssessmentResponseDTO assessment = assessmentService.createAssessment(CreateAssessmentRequest.builder()
                .userId(testUser.getId())
                .assessmentType(AssessmentType.INITIAL)
                .build());

        // Q1 (Budgeting, Beginner): Correct
        submitCorrectAnswer(assessment.getId(), qBudgetingBeginner);
        // Q2 (Saving, Intermediate): Incorrect
        submitIncorrectAnswer(assessment.getId(), qSavingIntermediate);

        assessmentCompletionService.completeAssessment(assessment.getId());

        AssessmentAnalyticsResponse analytics = assessmentAnalyticsService.getAssessmentAnalytics(assessment.getId());

        assertThat(analytics.getTotalQuestions()).isEqualTo(2);
        assertThat(analytics.getCorrectAnswers()).isEqualTo(1);
        assertThat(analytics.getIncorrectAnswers()).isEqualTo(1);
        assertThat(analytics.getOverallPercentage()).isEqualTo(50.0);
        assertThat(analytics.getProficiencyLevel()).isEqualTo("INTERMEDIATE");

        // Verify Difficulty features
        AssessmentFeatureVector fv = analytics.getFeatureVector();
        assertThat(fv.getDifficultyFeatures().get(DifficultyLevel.BEGINNER).getPercentage()).isEqualTo(100.0);
        assertThat(fv.getDifficultyFeatures().get(DifficultyLevel.INTERMEDIATE).getPercentage()).isEqualTo(0.0);
        // ADVANCED was not included in this assessment, so its percentage should be null
        assertThat(fv.getDifficultyFeatures().get(DifficultyLevel.ADVANCED).getPercentage()).isNull();
        assertThat(fv.getDifficultyFeatures().get(DifficultyLevel.ADVANCED).getQuestionCount()).isEqualTo(0);
    }

    private void submitCorrectAnswer(UUID assessmentId, QuestionResponse question) {
        QuestionOptionResponse correctOpt = question.getOptions().stream()
                .filter(opt -> getIsCorrectFromService(question.getId(), opt.getId()))
                .findFirst()
                .orElseThrow();

        assessmentResponseService.submitAnswer(assessmentId, SubmitAnswerRequest.builder()
                .questionId(question.getId())
                .selectedOptionId(correctOpt.getId())
                .build());
    }

    private void submitIncorrectAnswer(UUID assessmentId, QuestionResponse question) {
        QuestionOptionResponse incorrectOpt = question.getOptions().stream()
                .filter(opt -> !getIsCorrectFromService(question.getId(), opt.getId()))
                .findFirst()
                .orElseThrow();

        assessmentResponseService.submitAnswer(assessmentId, SubmitAnswerRequest.builder()
                .questionId(question.getId())
                .selectedOptionId(incorrectOpt.getId())
                .build());
    }

    @Autowired
    private com.financialliteracy.repository.QuestionOptionRepository questionOptionRepository;

    private boolean getIsCorrectFromService(UUID questionId, UUID optionId) {
        return questionOptionRepository.findById(optionId)
                .map(com.financialliteracy.entity.QuestionOption::getIsCorrect)
                .orElse(false);
    }
}
