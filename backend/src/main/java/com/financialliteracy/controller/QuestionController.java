package com.financialliteracy.controller;

import com.financialliteracy.dto.common.ApiResponse;
import com.financialliteracy.dto.question.CreateQuestionRequest;
import com.financialliteracy.dto.question.QuestionResponse;
import com.financialliteracy.dto.question.UpdateQuestionRequest;
import com.financialliteracy.enums.DifficultyLevel;
import com.financialliteracy.enums.FinancialLiteracyCategory;
import com.financialliteracy.enums.QuestionType;
import com.financialliteracy.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    public ResponseEntity<ApiResponse<QuestionResponse>> createQuestion(@Valid @RequestBody CreateQuestionRequest request) {
        QuestionResponse response = questionService.createQuestion(request);
        return new ResponseEntity<>(ApiResponse.success("Question created successfully", response), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getQuestions(
            @RequestParam(required = false) FinancialLiteracyCategory category,
            @RequestParam(required = false) DifficultyLevel difficulty,
            @RequestParam(required = false) QuestionType questionType,
            @RequestParam(required = false) Boolean active) {
        List<QuestionResponse> questions = questionService.getQuestions(category, difficulty, questionType, active);
        return ResponseEntity.ok(ApiResponse.success(questions));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuestionResponse>> getQuestionById(@PathVariable UUID id) {
        QuestionResponse response = questionService.getQuestionById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<QuestionResponse>> updateQuestion(@PathVariable UUID id, @RequestBody UpdateQuestionRequest request) {
        QuestionResponse response = questionService.updateQuestion(id, request);
        return ResponseEntity.ok(ApiResponse.success("Question updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<QuestionResponse>> setQuestionStatus(@PathVariable UUID id, @RequestParam boolean active) {
        QuestionResponse response = questionService.setQuestionStatus(id, active);
        return ResponseEntity.ok(ApiResponse.success("Question status updated successfully", response));
    }
}
