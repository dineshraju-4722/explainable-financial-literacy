package com.financialliteracy.repository;

import com.financialliteracy.entity.*;
import com.financialliteracy.enums.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class DomainModelPersistenceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionOptionRepository questionOptionRepository;

    @Autowired
    private AssessmentResponseRepository assessmentResponseRepository;

    @Autowired
    private AssessmentResultRepository assessmentResultRepository;

    @Test
    @DisplayName("Verify persistence lifecycle of User, Assessment, Question, and Responses")
    void testDomainEntityPersistence() {
        // 1. Persist User
        User user = User.builder()
                .name("Test Student")
                .email("student@university.edu")
                .age(21)
                .educationLevel("Undergraduate")
                .occupation("Student")
                .build();
        User savedUser = userRepository.save(user);
        assertThat(savedUser.getId()).isNotNull();

        // 2. Persist Assessment
        Assessment assessment = Assessment.builder()
                .user(savedUser)
                .assessmentType(AssessmentType.INITIAL)
                .status(AssessmentStatus.IN_PROGRESS)
                .build();
        Assessment savedAssessment = assessmentRepository.save(assessment);
        assertThat(savedAssessment.getId()).isNotNull();

        // 3. Persist Question
        Question question = Question.builder()
                .questionText("What is the primary benefit of compound interest?")
                .category(FinancialLiteracyCategory.INTEREST)
                .difficulty(DifficultyLevel.BEGINNER)
                .questionType(QuestionType.MULTIPLE_CHOICE)
                .active(true)
                .build();
        Question savedQuestion = questionRepository.save(question);
        assertThat(savedQuestion.getId()).isNotNull();

        // 4. Persist Question Option
        QuestionOption option = QuestionOption.builder()
                .question(savedQuestion)
                .optionText("Interest earns interest over time")
                .optionOrder(1)
                .isCorrect(true)
                .build();
        QuestionOption savedOption = questionOptionRepository.save(option);
        assertThat(savedOption.getId()).isNotNull();

        // 5. Persist Assessment Response
        AssessmentResponse response = AssessmentResponse.builder()
                .assessment(savedAssessment)
                .question(savedQuestion)
                .selectedOption(savedOption)
                .answeredAt(Instant.now())
                .build();
        AssessmentResponse savedResponse = assessmentResponseRepository.save(response);
        assertThat(savedResponse.getId()).isNotNull();

        // 6. Persist Assessment Result
        AssessmentResult result = AssessmentResult.builder()
                .assessment(savedAssessment)
                .totalScore(80.0)
                .percentage(80.0)
                .proficiencyLevel("HIGH")
                .build();
        AssessmentResult savedResult = assessmentResultRepository.save(result);
        assertThat(savedResult.getId()).isNotNull();

        // Verification queries
        Optional<User> retrievedUser = userRepository.findByEmail("student@university.edu");
        assertThat(retrievedUser).isPresent();
        assertThat(retrievedUser.get().getName()).isEqualTo("Test Student");

        Optional<AssessmentResult> retrievedResult = assessmentResultRepository.findByAssessmentId(savedAssessment.getId());
        assertThat(retrievedResult).isPresent();
        assertThat(retrievedResult.get().getPercentage()).isEqualTo(80.0);
    }
}
