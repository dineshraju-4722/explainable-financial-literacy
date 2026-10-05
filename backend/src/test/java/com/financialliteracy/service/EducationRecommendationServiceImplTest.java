package com.financialliteracy.service;

import com.financialliteracy.dto.assessment.AssessmentResponseDTO;
import com.financialliteracy.dto.assessment.CreateAssessmentRequest;
import com.financialliteracy.dto.assessment.SubmitAnswerRequest;

import com.financialliteracy.dto.education.EducationRecommendationDTO;
import com.financialliteracy.dto.education.EducationRecommendationResponse;
import com.financialliteracy.dto.question.CreateQuestionOptionRequest;
import com.financialliteracy.dto.question.CreateQuestionRequest;
import com.financialliteracy.dto.question.QuestionResponse;
import com.financialliteracy.dto.user.CreateUserRequest;
import com.financialliteracy.dto.user.UserResponse;
import com.financialliteracy.entity.AssessmentResult;
import com.financialliteracy.enums.*;
import com.financialliteracy.exception.BadRequestException;
import com.financialliteracy.exception.ResourceNotFoundException;
import com.financialliteracy.repository.AssessmentResultRepository;
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
class EducationRecommendationServiceImplTest {

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

    @Autowired
    private EducationRecommendationService educationRecommendationService;

    private UserResponse user;
    private QuestionResponse qBudgeting;
    private QuestionResponse qSaving;
    private QuestionResponse qCredit;

    @BeforeEach
    void setUp() {
        user = userService.createUser(CreateUserRequest.builder()
                .name("Jordan Lee")
                .email("jordan.lee@example.com")
                .age(28)
                .educationLevel("Bachelor")
                .occupation("Analyst")
                .build());

        qBudgeting = questionService.createQuestion(CreateQuestionRequest.builder()
                .questionText("What is the first step in creating a budget?")
                .category(FinancialLiteracyCategory.BUDGETING)
                .difficulty(DifficultyLevel.BEGINNER)
                .questionType(QuestionType.MULTIPLE_CHOICE)
                .options(List.of(
                        CreateQuestionOptionRequest.builder().optionText("Track income and expenses").optionOrder(1).isCorrect(true).build(),
                        CreateQuestionOptionRequest.builder().optionText("Buy stocks").optionOrder(2).isCorrect(false).build()
                ))
                .build());

        qSaving = questionService.createQuestion(CreateQuestionRequest.builder()
                .questionText("How many months of living expenses should an emergency fund cover?")
                .category(FinancialLiteracyCategory.SAVING)
                .difficulty(DifficultyLevel.BEGINNER)
                .questionType(QuestionType.MULTIPLE_CHOICE)
                .options(List.of(
                        CreateQuestionOptionRequest.builder().optionText("3 to 6 months").optionOrder(1).isCorrect(true).build(),
                        CreateQuestionOptionRequest.builder().optionText("1 day").optionOrder(2).isCorrect(false).build()
                ))
                .build());

        qCredit = questionService.createQuestion(CreateQuestionRequest.builder()
                .questionText("What factor has the largest impact on your credit score?")
                .category(FinancialLiteracyCategory.CREDIT)
                .difficulty(DifficultyLevel.BEGINNER)
                .questionType(QuestionType.MULTIPLE_CHOICE)
                .options(List.of(
                        CreateQuestionOptionRequest.builder().optionText("Payment history").optionOrder(1).isCorrect(true).build(),
                        CreateQuestionOptionRequest.builder().optionText("Favorite color").optionOrder(2).isCorrect(false).build()
                ))
                .build());
    }

    @Test
    @DisplayName("Generate Recommendations for UNDERCONFIDENT archetype with mixed performance")
    void testUnderconfidentRecommendations() {
        AssessmentResponseDTO assessment = assessmentService.createAssessment(CreateAssessmentRequest.builder()
                .userId(user.getId())
                .assessmentType(AssessmentType.INITIAL)
                .build());

        // Answer Budgeting correctly (100%), Saving incorrectly (0%), Credit correctly (100%)
        assessmentResponseService.submitAnswer(assessment.getId(), SubmitAnswerRequest.builder()
                .questionId(qBudgeting.getId())
                .selectedOptionId(qBudgeting.getOptions().get(0).getId())
                .build());

        assessmentResponseService.submitAnswer(assessment.getId(), SubmitAnswerRequest.builder()
                .questionId(qSaving.getId())
                .selectedOptionId(qSaving.getOptions().get(1).getId())
                .build());

        assessmentResponseService.submitAnswer(assessment.getId(), SubmitAnswerRequest.builder()
                .questionId(qCredit.getId())
                .selectedOptionId(qCredit.getOptions().get(0).getId())
                .build());

        assessmentCompletionService.completeAssessment(assessment.getId());

        // Set ML Archetype to UNDERCONFIDENT in database
        AssessmentResult result = assessmentResultRepository.findByAssessmentId(assessment.getId()).orElseThrow();
        result.setMlArchetype(MlArchetype.UNDERCONFIDENT);
        assessmentResultRepository.save(result);

        EducationRecommendationResponse response = educationRecommendationService.generateRecommendations(assessment.getId());

        assertThat(response).isNotNull();
        assertThat(response.getAssessmentId()).isEqualTo(assessment.getId());
        assertThat(response.getArchetype()).isEqualTo(MlArchetype.UNDERCONFIDENT);
        assertThat(response.getArchetypeSummary()).contains("stronger demonstrated performance than your self-assessed financial knowledge");
        assertThat(response.getRecommendations()).isNotEmpty();

        // Verify priorities: SAVING (0%) should be HIGH priority FOUNDATION recommendation
        EducationRecommendationDTO firstRec = response.getRecommendations().get(0);
        assertThat(firstRec.getPriority()).isEqualTo(RecommendationPriority.HIGH);
        assertThat(firstRec.getTopic()).isEqualTo(FinancialLiteracyCategory.SAVING);
        assertThat(firstRec.getRecommendationType()).isEqualTo(RecommendationType.FOUNDATION);

        // Verify CONFIDENCE_BUILDING recommendation type present for strong topics in UNDERCONFIDENT archetype
        boolean hasConfidenceBuilding = response.getRecommendations().stream()
                .anyMatch(r -> r.getRecommendationType() == RecommendationType.CONFIDENCE_BUILDING);
        assertThat(hasConfidenceBuilding).isTrue();
    }

    @Test
    @DisplayName("Generate Recommendations for COMPETENT archetype with strong performance")
    void testCompetentRecommendations() {
        AssessmentResponseDTO assessment = assessmentService.createAssessment(CreateAssessmentRequest.builder()
                .userId(user.getId())
                .assessmentType(AssessmentType.INITIAL)
                .build());

        // Answer all questions correctly
        assessmentResponseService.submitAnswer(assessment.getId(), SubmitAnswerRequest.builder()
                .questionId(qBudgeting.getId())
                .selectedOptionId(qBudgeting.getOptions().get(0).getId())
                .build());

        assessmentResponseService.submitAnswer(assessment.getId(), SubmitAnswerRequest.builder()
                .questionId(qSaving.getId())
                .selectedOptionId(qSaving.getOptions().get(0).getId())
                .build());

        assessmentCompletionService.completeAssessment(assessment.getId());

        AssessmentResult result = assessmentResultRepository.findByAssessmentId(assessment.getId()).orElseThrow();
        result.setMlArchetype(MlArchetype.COMPETENT);
        assessmentResultRepository.save(result);

        EducationRecommendationResponse response = educationRecommendationService.generateRecommendations(assessment.getId());

        assertThat(response.getArchetype()).isEqualTo(MlArchetype.COMPETENT);
        assertThat(response.getArchetypeSummary()).contains("relatively strong financial literacy capability");
        assertThat(response.getRecommendations()).isNotEmpty();

        // COMPETENT + strong performance -> ADVANCED or REINFORCEMENT recommendation type
        boolean hasAdvanced = response.getRecommendations().stream()
                .anyMatch(r -> r.getRecommendationType() == RecommendationType.ADVANCED);
        assertThat(hasAdvanced).isTrue();
    }

    @Test
    @DisplayName("Generate Recommendations for OVERCONFIDENT archetype with weak performance")
    void testOverconfidentRecommendations() {
        AssessmentResponseDTO assessment = assessmentService.createAssessment(CreateAssessmentRequest.builder()
                .userId(user.getId())
                .assessmentType(AssessmentType.INITIAL)
                .build());

        // Answer questions incorrectly
        assessmentResponseService.submitAnswer(assessment.getId(), SubmitAnswerRequest.builder()
                .questionId(qBudgeting.getId())
                .selectedOptionId(qBudgeting.getOptions().get(1).getId())
                .build());

        assessmentCompletionService.completeAssessment(assessment.getId());

        AssessmentResult result = assessmentResultRepository.findByAssessmentId(assessment.getId()).orElseThrow();
        result.setMlArchetype(MlArchetype.OVERCONFIDENT);
        assessmentResultRepository.save(result);

        EducationRecommendationResponse response = educationRecommendationService.generateRecommendations(assessment.getId());

        assertThat(response.getArchetype()).isEqualTo(MlArchetype.OVERCONFIDENT);
        assertThat(response.getArchetypeSummary()).contains("self-assessed financial knowledge is higher than your demonstrated performance");
        
        // Weak performance in foundational topic -> HIGH priority FOUNDATION recommendation
        EducationRecommendationDTO rec = response.getRecommendations().get(0);
        assertThat(rec.getPriority()).isEqualTo(RecommendationPriority.HIGH);
        assertThat(rec.getRecommendationType()).isEqualTo(RecommendationType.FOUNDATION);
    }

    @Test
    @DisplayName("Generate Recommendations for NAIVE archetype")
    void testNaiveRecommendations() {
        AssessmentResponseDTO assessment = assessmentService.createAssessment(CreateAssessmentRequest.builder()
                .userId(user.getId())
                .assessmentType(AssessmentType.INITIAL)
                .build());

        assessmentResponseService.submitAnswer(assessment.getId(), SubmitAnswerRequest.builder()
                .questionId(qBudgeting.getId())
                .selectedOptionId(qBudgeting.getOptions().get(1).getId())
                .build());

        assessmentCompletionService.completeAssessment(assessment.getId());

        AssessmentResult result = assessmentResultRepository.findByAssessmentId(assessment.getId()).orElseThrow();
        result.setMlArchetype(MlArchetype.NAIVE);
        assessmentResultRepository.save(result);

        EducationRecommendationResponse response = educationRecommendationService.generateRecommendations(assessment.getId());

        assertThat(response.getArchetype()).isEqualTo(MlArchetype.NAIVE);
        assertThat(response.getArchetypeSummary()).contains("opportunities to strengthen foundational financial concepts");
    }

    @Test
    @DisplayName("Verify Determinism: Two calls for exact same assessment yield identical results")
    void testDeterminism() {
        AssessmentResponseDTO assessment = assessmentService.createAssessment(CreateAssessmentRequest.builder()
                .userId(user.getId())
                .assessmentType(AssessmentType.INITIAL)
                .build());

        assessmentResponseService.submitAnswer(assessment.getId(), SubmitAnswerRequest.builder()
                .questionId(qBudgeting.getId())
                .selectedOptionId(qBudgeting.getOptions().get(0).getId())
                .build());

        assessmentCompletionService.completeAssessment(assessment.getId());

        AssessmentResult result = assessmentResultRepository.findByAssessmentId(assessment.getId()).orElseThrow();
        result.setMlArchetype(MlArchetype.COMPETENT);
        assessmentResultRepository.save(result);

        EducationRecommendationResponse call1 = educationRecommendationService.generateRecommendations(assessment.getId());
        EducationRecommendationResponse call2 = educationRecommendationService.generateRecommendations(assessment.getId());

        assertThat(call1.getAssessmentId()).isEqualTo(call2.getAssessmentId());
        assertThat(call1.getArchetype()).isEqualTo(call2.getArchetype());
        assertThat(call1.getTotalRecommendations()).isEqualTo(call2.getTotalRecommendations());

        for (int i = 0; i < call1.getRecommendations().size(); i++) {
            EducationRecommendationDTO r1 = call1.getRecommendations().get(i);
            EducationRecommendationDTO r2 = call2.getRecommendations().get(i);
            assertThat(r1.getRecommendationId()).isEqualTo(r2.getRecommendationId());
            assertThat(r1.getContentId()).isEqualTo(r2.getContentId());
            assertThat(r1.getPriority()).isEqualTo(r2.getPriority());
            assertThat(r1.getTopic()).isEqualTo(r2.getTopic());
            assertThat(r1.getTitle()).isEqualTo(r2.getTitle());
        }
    }

    @Test
    @DisplayName("Verify Safety Boundaries: No investment advice or product recommendations")
    void testSafetyBoundaries() {
        AssessmentResponseDTO assessment = assessmentService.createAssessment(CreateAssessmentRequest.builder()
                .userId(user.getId())
                .assessmentType(AssessmentType.INITIAL)
                .build());

        assessmentResponseService.submitAnswer(assessment.getId(), SubmitAnswerRequest.builder()
                .questionId(qBudgeting.getId())
                .selectedOptionId(qBudgeting.getOptions().get(0).getId())
                .build());

        assessmentCompletionService.completeAssessment(assessment.getId());

        AssessmentResult result = assessmentResultRepository.findByAssessmentId(assessment.getId()).orElseThrow();
        result.setMlArchetype(MlArchetype.COMPETENT);
        assessmentResultRepository.save(result);

        EducationRecommendationResponse response = educationRecommendationService.generateRecommendations(assessment.getId());

        for (EducationRecommendationDTO rec : response.getRecommendations()) {
            String combinedText = (rec.getTitle() + " " + rec.getShortDescription() + " " + rec.getRationale() + " " + rec.getLearningObjective()).toLowerCase();

            assertThat(combinedText).doesNotContain("buy stock");
            assertThat(combinedText).doesNotContain("invest 20%");
            assertThat(combinedText).doesNotContain("take a personal loan");
            assertThat(combinedText).doesNotContain("purchase insurance policy");
            assertThat(combinedText).doesNotContain("caused by shap");
        }
    }

    @Test
    @DisplayName("Throw ResourceNotFoundException for non-existent assessment ID")
    void testNonExistentAssessmentId() {
        UUID fakeId = UUID.randomUUID();
        assertThatThrownBy(() -> educationRecommendationService.generateRecommendations(fakeId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Assessment not found with id: " + fakeId);
    }

    @Test
    @DisplayName("Throw BadRequestException for incomplete assessment")
    void testIncompleteAssessment() {
        AssessmentResponseDTO assessment = assessmentService.createAssessment(CreateAssessmentRequest.builder()
                .userId(user.getId())
                .assessmentType(AssessmentType.INITIAL)
                .build());

        assertThatThrownBy(() -> educationRecommendationService.generateRecommendations(assessment.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot generate recommendations for an incomplete assessment");
    }
}
