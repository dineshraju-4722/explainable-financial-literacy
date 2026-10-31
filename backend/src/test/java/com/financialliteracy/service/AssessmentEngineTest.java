package com.financialliteracy.service;

import com.financialliteracy.dto.assessment.*;
import com.financialliteracy.dto.question.*;
import com.financialliteracy.dto.user.CreateUserRequest;
import com.financialliteracy.dto.user.UserResponse;
import com.financialliteracy.enums.*;
import com.financialliteracy.exception.IllegalStateTransitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AssessmentEngineTest {

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

    @Test
    @DisplayName("Complete end-to-end assessment workflow test")
    void testEndToEndAssessmentWorkflow() {
        // 1. Create User
        UserResponse user = userService.createUser(CreateUserRequest.builder()
                .name("Alex Smith")
                .email("alex.smith@university.edu")
                .age(22)
                .educationLevel("Undergraduate")
                .occupation("Student")
                .build());
        assertThat(user.getId()).isNotNull();

        // 2. Create Question 1 (Budgeting)
        QuestionResponse q1 = questionService.createQuestion(CreateQuestionRequest.builder()
                .questionText("What percentage of income goes to Needs in the 50/30/20 rule?")
                .category(FinancialLiteracyCategory.BUDGETING)
                .difficulty(DifficultyLevel.BEGINNER)
                .questionType(QuestionType.MULTIPLE_CHOICE)
                .options(List.of(
                        CreateQuestionOptionRequest.builder().optionText("50%").optionOrder(1).isCorrect(true).build(),
                        CreateQuestionOptionRequest.builder().optionText("30%").optionOrder(2).isCorrect(false).build(),
                        CreateQuestionOptionRequest.builder().optionText("20%").optionOrder(3).isCorrect(false).build()
                ))
                .build());

        // Create Question 2 (Interest)
        QuestionResponse q2 = questionService.createQuestion(CreateQuestionRequest.builder()
                .questionText("Compound interest calculates interest on principal plus accumulated interest?")
                .category(FinancialLiteracyCategory.INTEREST)
                .difficulty(DifficultyLevel.BEGINNER)
                .questionType(QuestionType.TRUE_FALSE)
                .options(List.of(
                        CreateQuestionOptionRequest.builder().optionText("True").optionOrder(1).isCorrect(true).build(),
                        CreateQuestionOptionRequest.builder().optionText("False").optionOrder(2).isCorrect(false).build()
                ))
                .build());

        // 3. Create Assessment Attempt
        AssessmentResponseDTO assessment = assessmentService.createAssessment(CreateAssessmentRequest.builder()
                .userId(user.getId())
                .assessmentType(AssessmentType.INITIAL)
                .build());
        assertThat(assessment.getStatus()).isEqualTo(AssessmentStatus.IN_PROGRESS);

        // 4. Retrieve Questions for Assessment and verify Security Rule
        List<QuestionResponse> questions = assessmentService.getAssessmentQuestions(assessment.getId());
        assertThat(questions).hasSizeGreaterThanOrEqualTo(2);
        // Ensure options do not expose correctness
        for (QuestionResponse q : questions) {
            for (QuestionOptionResponse opt : q.getOptions()) {
                assertThat(opt.getId()).isNotNull();
                assertThat(opt.getOptionText()).isNotNull();
            }
        }

        // 5. Submit Answers
        // Answer Q1 correctly (50% -> option index 0)
        QuestionOptionResponse q1CorrectOpt = q1.getOptions().get(0);
        SubmitAnswerResponse resp1 = assessmentResponseService.submitAnswer(assessment.getId(), SubmitAnswerRequest.builder()
                .questionId(q1.getId())
                .selectedOptionId(q1CorrectOpt.getId())
                .build());
        assertThat(resp1.isUpdatedExisting()).isFalse();

        // Answer Q2 incorrectly (False -> option index 1)
        QuestionOptionResponse q2IncorrectOpt = q2.getOptions().get(1);
        assessmentResponseService.submitAnswer(assessment.getId(), SubmitAnswerRequest.builder()
                .questionId(q2.getId())
                .selectedOptionId(q2IncorrectOpt.getId())
                .build());

        // 6. Test Answer Updating (change Q2 answer to True -> option index 0)
        QuestionOptionResponse q2CorrectOpt = q2.getOptions().get(0);
        SubmitAnswerResponse resp2Updated = assessmentResponseService.submitAnswer(assessment.getId(), SubmitAnswerRequest.builder()
                .questionId(q2.getId())
                .selectedOptionId(q2CorrectOpt.getId())
                .build());
        assertThat(resp2Updated.isUpdatedExisting()).isTrue();

        // 7. Complete Assessment
        AssessmentResultResponse result = assessmentCompletionService.completeAssessment(assessment.getId());
        assertThat(result.getAssessmentId()).isEqualTo(assessment.getId());
        assertThat(result.getTotalQuestions()).isEqualTo(2);
        assertThat(result.getCorrectAnswers()).isEqualTo(2);
        assertThat(result.getTotalScore()).isEqualTo(2.0);
        assertThat(result.getPercentage()).isEqualTo(100.0);
        assertThat(result.getProficiencyLevel()).isEqualTo("ADVANCED");

        // Verify Assessment status transitioned to COMPLETED
        AssessmentResponseDTO completedAssessment = assessmentService.getAssessmentById(assessment.getId());
        assertThat(completedAssessment.getStatus()).isEqualTo(AssessmentStatus.COMPLETED);

        // 8. Attempt second completion -> Should throw IllegalStateTransitionException
        assertThatThrownBy(() -> assessmentCompletionService.completeAssessment(assessment.getId()))
                .isInstanceOf(IllegalStateTransitionException.class)
                .hasMessageContaining("Cannot complete assessment. Current status is already: COMPLETED");
    }
}
