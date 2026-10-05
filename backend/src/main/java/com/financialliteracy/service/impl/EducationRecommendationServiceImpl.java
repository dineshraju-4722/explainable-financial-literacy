package com.financialliteracy.service.impl;

import com.financialliteracy.dto.education.EducationRecommendationDTO;
import com.financialliteracy.dto.education.EducationRecommendationResponse;
import com.financialliteracy.dto.education.EducationalNeedDTO;
import com.financialliteracy.dto.education.EducationalNeedProfileDTO;
import com.financialliteracy.entity.Assessment;
import com.financialliteracy.entity.AssessmentResponse;
import com.financialliteracy.entity.AssessmentResult;
import com.financialliteracy.entity.EducationalContent;
import com.financialliteracy.enums.AssessmentStatus;
import com.financialliteracy.enums.FinancialLiteracyCategory;
import com.financialliteracy.enums.MlArchetype;
import com.financialliteracy.enums.RecommendationPriority;
import com.financialliteracy.enums.RecommendationType;
import com.financialliteracy.enums.TopicPerformanceLevel;
import com.financialliteracy.exception.BadRequestException;
import com.financialliteracy.exception.MlServiceUnavailableException;
import com.financialliteracy.exception.ResourceNotFoundException;
import com.financialliteracy.repository.AssessmentRepository;
import com.financialliteracy.repository.AssessmentResponseRepository;
import com.financialliteracy.repository.AssessmentResultRepository;
import com.financialliteracy.repository.EducationalContentRepository;
import com.financialliteracy.service.EducationRecommendationService;
import com.financialliteracy.service.MlPredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EducationRecommendationServiceImpl implements EducationRecommendationService {

    private final AssessmentRepository assessmentRepository;
    private final AssessmentResponseRepository assessmentResponseRepository;
    private final AssessmentResultRepository assessmentResultRepository;
    private final EducationalContentRepository educationalContentRepository;
    private final MlPredictionService mlPredictionService;

    @Override
    @Transactional(readOnly = true)
    public EducationRecommendationResponse generateRecommendations(UUID assessmentId) {
        // 1. Fetch & Validate Assessment
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + assessmentId));

        if (assessment.getStatus() != AssessmentStatus.COMPLETED) {
            throw new BadRequestException("Cannot generate recommendations for an incomplete assessment");
        }

        // 2. Obtain ML Archetype from AssessmentResult
        Optional<AssessmentResult> resultOpt = assessmentResultRepository.findByAssessmentId(assessmentId);
        MlArchetype archetype = null;

        if (resultOpt.isPresent() && resultOpt.get().getMlArchetype() != null) {
            archetype = resultOpt.get().getMlArchetype();
        } else {
            throw new MlServiceUnavailableException("ML Archetype unavailable for assessment: " + assessmentId);
        }

        // 3. Fetch Assessment Responses & Calculate Topic Performance
        List<AssessmentResponse> responses = assessmentResponseRepository.findByAssessmentId(assessmentId);
        if (responses.isEmpty()) {
            throw new BadRequestException("Cannot generate recommendations for an assessment with zero responses");
        }

        Map<FinancialLiteracyCategory, List<AssessmentResponse>> responsesByCategory = responses.stream()
                .filter(resp -> resp.getQuestion() != null && resp.getQuestion().getCategory() != null)
                .collect(Collectors.groupingBy(resp -> resp.getQuestion().getCategory()));

        List<EducationalNeedDTO> needs = new ArrayList<>();

        for (Map.Entry<FinancialLiteracyCategory, List<AssessmentResponse>> entry : responsesByCategory.entrySet()) {
            FinancialLiteracyCategory category = entry.getKey();
            List<AssessmentResponse> categoryResponses = entry.getValue();

            int total = categoryResponses.size();
            int correct = (int) categoryResponses.stream()
                    .filter(resp -> resp.getSelectedOption() != null && Boolean.TRUE.equals(resp.getSelectedOption().getIsCorrect()))
                    .count();

            double scorePercentage = ((double) correct / total) * 100.0;

            TopicPerformanceLevel performanceLevel;
            if (scorePercentage < 40.0) {
                performanceLevel = TopicPerformanceLevel.NEEDS_FOUNDATION;
            } else if (scorePercentage < 70.0) {
                performanceLevel = TopicPerformanceLevel.NEEDS_REINFORCEMENT;
            } else {
                performanceLevel = TopicPerformanceLevel.STRONG;
            }

            RecommendationPriority priority = derivePriority(archetype, category, performanceLevel);
            RecommendationType recommendationType = deriveRecommendationType(archetype, performanceLevel);
            String rationale = deriveRationale(archetype, category, scorePercentage, performanceLevel, recommendationType);
            String learningObjective = "Strengthen understanding and capability in " + formatTopicName(category);

            needs.add(EducationalNeedDTO.builder()
                    .topic(category)
                    .topicScorePercentage(scorePercentage)
                    .totalQuestions(total)
                    .correctQuestions(correct)
                    .performanceLevel(performanceLevel)
                    .priority(priority)
                    .recommendationType(recommendationType)
                    .rationale(rationale)
                    .learningObjective(learningObjective)
                    .build());
        }

        String archetypeSummary = deriveArchetypeSummary(archetype);

        EducationalNeedProfileDTO profile = EducationalNeedProfileDTO.builder()
                .assessmentId(assessmentId)
                .archetype(archetype)
                .archetypeSummary(archetypeSummary)
                .needs(needs)
                .build();

        // 4. Match Needs to Curated Educational Content & Sort Deterministically
        List<EducationRecommendationDTO> recommendations = new ArrayList<>();
        Set<UUID> addedContentIds = new HashSet<>();

        for (EducationalNeedDTO need : profile.getNeeds()) {
            List<EducationalContent> contents = educationalContentRepository.findByTopicAndActiveTrue(need.getTopic());

            for (EducationalContent content : contents) {
                if (addedContentIds.add(content.getId())) {
                    UUID recId = UUID.nameUUIDFromBytes((assessmentId.toString() + ":" + content.getId().toString()).getBytes(StandardCharsets.UTF_8));

                    recommendations.add(EducationRecommendationDTO.builder()
                            .recommendationId(recId)
                            .contentId(content.getId())
                            .topic(need.getTopic())
                            .priority(need.getPriority())
                            .recommendationType(need.getRecommendationType())
                            .title(content.getTitle())
                            .shortDescription(content.getShortDescription())
                            .rationale(need.getRationale())
                            .learningObjective(content.getLearningObjectives())
                            .estimatedDurationMinutes(content.getEstimatedDurationMinutes())
                            .difficulty(content.getDifficulty())
                            .build());
                }
            }
        }

        // If no specific recommendations were generated, add fallback curated content for foundational topics
        if (recommendations.isEmpty()) {
            List<EducationalContent> allContents = educationalContentRepository.findByActiveTrue();
            for (EducationalContent content : allContents) {
                if (addedContentIds.add(content.getId())) {
                    UUID recId = UUID.nameUUIDFromBytes((assessmentId.toString() + ":" + content.getId().toString()).getBytes(StandardCharsets.UTF_8));
                    recommendations.add(EducationRecommendationDTO.builder()
                            .recommendationId(recId)
                            .contentId(content.getId())
                            .topic(content.getTopic())
                            .priority(RecommendationPriority.MEDIUM)
                            .recommendationType(RecommendationType.REINFORCEMENT)
                            .title(content.getTitle())
                            .shortDescription(content.getShortDescription())
                            .rationale("Recommended content to reinforce general financial literacy based on your assessment results.")
                            .learningObjective(content.getLearningObjectives())
                            .estimatedDurationMinutes(content.getEstimatedDurationMinutes())
                            .difficulty(content.getDifficulty())
                            .build());
                }
            }
        }

        // 5. Sort Recommendations Deterministically
        // Ordering: 1. Priority (HIGH < MEDIUM < LOW), 2. Topic score percentage asc, 3. Topic name asc, 4. Title asc
        Map<FinancialLiteracyCategory, Double> topicScores = needs.stream()
                .collect(Collectors.toMap(EducationalNeedDTO::getTopic, EducationalNeedDTO::getTopicScorePercentage, (a, b) -> a));

        recommendations.sort((r1, r2) -> {
            int pComp = Integer.compare(r1.getPriority().ordinal(), r2.getPriority().ordinal());
            if (pComp != 0) return pComp;

            double s1 = topicScores.getOrDefault(r1.getTopic(), 100.0);
            double s2 = topicScores.getOrDefault(r2.getTopic(), 100.0);
            int scoreComp = Double.compare(s1, s2);
            if (scoreComp != 0) return scoreComp;

            int topicComp = r1.getTopic().name().compareTo(r2.getTopic().name());
            if (topicComp != 0) return topicComp;

            return r1.getTitle().compareTo(r2.getTitle());
        });

        return EducationRecommendationResponse.builder()
                .assessmentId(assessmentId)
                .archetype(archetype)
                .archetypeSummary(archetypeSummary)
                .generatedAt(Instant.now().toString())
                .totalRecommendations(recommendations.size())
                .recommendations(recommendations)
                .build();
    }

    private RecommendationPriority derivePriority(MlArchetype archetype, FinancialLiteracyCategory category, TopicPerformanceLevel performanceLevel) {
        if (performanceLevel == TopicPerformanceLevel.NEEDS_FOUNDATION) {
            return RecommendationPriority.HIGH;
        }

        boolean isCoreTopic = isCoreFoundationalTopic(category);

        if ((archetype == MlArchetype.NAIVE || archetype == MlArchetype.OVERCONFIDENT) && isCoreTopic) {
            return RecommendationPriority.HIGH;
        }

        if (performanceLevel == TopicPerformanceLevel.NEEDS_REINFORCEMENT) {
            return RecommendationPriority.MEDIUM;
        }

        if (archetype == MlArchetype.UNDERCONFIDENT && performanceLevel == TopicPerformanceLevel.STRONG) {
            return RecommendationPriority.MEDIUM; // Confidence building priority
        }

        if (archetype == MlArchetype.COMPETENT && performanceLevel == TopicPerformanceLevel.STRONG) {
            return RecommendationPriority.LOW;
        }

        return RecommendationPriority.LOW;
    }

    private RecommendationType deriveRecommendationType(MlArchetype archetype, TopicPerformanceLevel performanceLevel) {
        if (performanceLevel == TopicPerformanceLevel.NEEDS_FOUNDATION) {
            return RecommendationType.FOUNDATION;
        }

        if (archetype == MlArchetype.UNDERCONFIDENT && performanceLevel == TopicPerformanceLevel.STRONG) {
            return RecommendationType.CONFIDENCE_BUILDING;
        }

        if (archetype == MlArchetype.COMPETENT && performanceLevel == TopicPerformanceLevel.STRONG) {
            return RecommendationType.ADVANCED;
        }

        return RecommendationType.REINFORCEMENT;
    }

    private String deriveRationale(MlArchetype archetype, FinancialLiteracyCategory category, double scorePercentage, TopicPerformanceLevel performanceLevel, RecommendationType recType) {
        String topicStr = formatTopicName(category);

        if (performanceLevel == TopicPerformanceLevel.NEEDS_FOUNDATION) {
            return String.format("Your assessment performance in %s (%.0f%%) indicates a fundamental need to strengthen core concepts.", topicStr, scorePercentage);
        }

        if (recType == RecommendationType.CONFIDENCE_BUILDING) {
            return String.format("Your demonstrated performance in %s (%.0f%%) is strong. Recommended content focuses on reinforcing your understanding to build confidence.", topicStr, scorePercentage);
        }

        if (recType == RecommendationType.ADVANCED) {
            return String.format("Your demonstrated performance in %s (%.0f%%) is strong. Advanced content is recommended to extend your capability.", topicStr, scorePercentage);
        }

        return String.format("Your assessment performance in %s (%.0f%%) suggests an opportunity to reinforce and consolidate your understanding.", topicStr, scorePercentage);
    }

    private String deriveArchetypeSummary(MlArchetype archetype) {
        return switch (archetype) {
            case COMPETENT -> "Your results indicate relatively strong financial literacy capability. The recommended content focuses on reinforcing and extending that capability.";
            case OVERCONFIDENT -> "Your assessment pattern suggests that your self-assessed financial knowledge is higher than your demonstrated performance on the assessment. The recommended content focuses on strengthening core concepts and validating financial assumptions.";
            case UNDERCONFIDENT -> "Your assessment pattern suggests stronger demonstrated performance than your self-assessed financial knowledge. The recommended content focuses on reinforcing your existing knowledge and building confidence in applying it.";
            case NAIVE -> "The assessment indicates opportunities to strengthen foundational financial concepts.";
        };
    }

    private boolean isCoreFoundationalTopic(FinancialLiteracyCategory category) {
        return category == FinancialLiteracyCategory.BUDGETING ||
               category == FinancialLiteracyCategory.SAVING ||
               category == FinancialLiteracyCategory.CREDIT ||
               category == FinancialLiteracyCategory.INTEREST ||
               category == FinancialLiteracyCategory.INFLATION ||
               category == FinancialLiteracyCategory.DEBT_MANAGEMENT;
    }

    private String formatTopicName(FinancialLiteracyCategory category) {
        return category.name().toLowerCase().replace('_', ' ');
    }
}
