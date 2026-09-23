package com.financialliteracy.service.impl;

import com.financialliteracy.dto.question.*;
import com.financialliteracy.entity.Question;
import com.financialliteracy.entity.QuestionOption;
import com.financialliteracy.enums.DifficultyLevel;
import com.financialliteracy.enums.FinancialLiteracyCategory;
import com.financialliteracy.enums.QuestionType;
import com.financialliteracy.exception.BadRequestException;
import com.financialliteracy.exception.ResourceNotFoundException;
import com.financialliteracy.repository.QuestionOptionRepository;
import com.financialliteracy.repository.QuestionRepository;
import com.financialliteracy.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;

    @Override
    @Transactional
    public QuestionResponse createQuestion(CreateQuestionRequest request) {
        validateOptions(request.getQuestionType(), request.getOptions());

        Question question = Question.builder()
                .questionText(request.getQuestionText())
                .category(request.getCategory())
                .difficulty(request.getDifficulty())
                .questionType(request.getQuestionType())
                .active(true)
                .build();

        Question savedQuestion = questionRepository.save(question);

        List<QuestionOption> options = new ArrayList<>();
        for (CreateQuestionOptionRequest optReq : request.getOptions()) {
            QuestionOption option = QuestionOption.builder()
                    .question(savedQuestion)
                    .optionText(optReq.getOptionText())
                    .optionOrder(optReq.getOptionOrder())
                    .isCorrect(optReq.getIsCorrect())
                    .build();
            options.add(option);
        }

        List<QuestionOption> savedOptions = questionOptionRepository.saveAll(options);
        return mapToResponse(savedQuestion, savedOptions);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionResponse getQuestionById(UUID id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + id));
        List<QuestionOption> options = questionOptionRepository.findByQuestionIdOrderByOptionOrderAsc(id);
        return mapToResponse(question, options);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponse> getQuestions(FinancialLiteracyCategory category, DifficultyLevel difficulty, QuestionType questionType, Boolean active) {
        List<Question> questions = questionRepository.findAll();

        return questions.stream()
                .filter(q -> category == null || q.getCategory() == category)
                .filter(q -> difficulty == null || q.getDifficulty() == difficulty)
                .filter(q -> questionType == null || q.getQuestionType() == questionType)
                .filter(q -> active == null || q.getActive().equals(active))
                .map(q -> {
                    List<QuestionOption> options = questionOptionRepository.findByQuestionIdOrderByOptionOrderAsc(q.getId());
                    return mapToResponse(q, options);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public QuestionResponse updateQuestion(UUID id, UpdateQuestionRequest request) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + id));

        if (request.getQuestionText() != null && !request.getQuestionText().isBlank()) {
            question.setQuestionText(request.getQuestionText());
        }
        if (request.getCategory() != null) {
            question.setCategory(request.getCategory());
        }
        if (request.getDifficulty() != null) {
            question.setDifficulty(request.getDifficulty());
        }
        if (request.getQuestionType() != null) {
            question.setQuestionType(request.getQuestionType());
        }
        if (request.getActive() != null) {
            question.setActive(request.getActive());
        }

        Question updatedQuestion = questionRepository.save(question);
        List<QuestionOption> options = questionOptionRepository.findByQuestionIdOrderByOptionOrderAsc(id);
        return mapToResponse(updatedQuestion, options);
    }

    @Override
    @Transactional
    public QuestionResponse setQuestionStatus(UUID id, boolean active) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + id));
        question.setActive(active);
        Question updatedQuestion = questionRepository.save(question);
        List<QuestionOption> options = questionOptionRepository.findByQuestionIdOrderByOptionOrderAsc(id);
        return mapToResponse(updatedQuestion, options);
    }

    private void validateOptions(QuestionType questionType, List<CreateQuestionOptionRequest> options) {
        if (options == null || options.isEmpty()) {
            throw new BadRequestException("Question options list cannot be empty");
        }

        if (questionType == QuestionType.TRUE_FALSE && options.size() != 2) {
            throw new BadRequestException("TRUE_FALSE questions must contain exactly 2 options");
        }

        if (questionType == QuestionType.MULTIPLE_CHOICE && options.size() < 2) {
            throw new BadRequestException("MULTIPLE_CHOICE questions must contain at least 2 options");
        }

        long correctCount = options.stream().filter(o -> Boolean.TRUE.equals(o.getIsCorrect())).count();
        if (correctCount != 1) {
            throw new BadRequestException("Question must contain exactly ONE correct option (found: " + correctCount + ")");
        }
    }

    private QuestionResponse mapToResponse(Question question, List<QuestionOption> options) {
        List<QuestionOptionResponse> optionResponses = options.stream()
                .map(o -> QuestionOptionResponse.builder()
                        .id(o.getId())
                        .optionText(o.getOptionText())
                        .optionOrder(o.getOptionOrder())
                        .build()) // isCorrect is intentionally NOT mapped for client security!
                .collect(Collectors.toList());

        return QuestionResponse.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .category(question.getCategory())
                .difficulty(question.getDifficulty())
                .questionType(question.getQuestionType())
                .active(question.getActive())
                .options(optionResponses)
                .build();
    }
}
