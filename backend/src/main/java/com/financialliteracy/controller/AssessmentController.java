package com.financialliteracy.controller;

import com.financialliteracy.dto.assessment.*;
import com.financialliteracy.dto.common.ApiResponse;
import com.financialliteracy.dto.question.QuestionResponse;
import com.financialliteracy.service.AssessmentCompletionService;
import com.financialliteracy.service.AssessmentResponseService;
import com.financialliteracy.service.AssessmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/assessments")
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentService assessmentService;
    private final AssessmentResponseService assessmentResponseService;
    private final AssessmentCompletionService assessmentCompletionService;

    @PostMapping
    public ResponseEntity<ApiResponse<AssessmentResponseDTO>> createAssessment(@Valid @RequestBody CreateAssessmentRequest request) {
        AssessmentResponseDTO response = assessmentService.createAssessment(request);
        return new ResponseEntity<>(ApiResponse.success("Assessment initialized successfully", response), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AssessmentResponseDTO>> getAssessmentById(@PathVariable UUID id) {
        AssessmentResponseDTO response = assessmentService.getAssessmentById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/questions")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getAssessmentQuestions(@PathVariable UUID id) {
        List<QuestionResponse> questions = assessmentService.getAssessmentQuestions(id);
        return ResponseEntity.ok(ApiResponse.success(questions));
    }

    @PostMapping("/{id}/responses")
    public ResponseEntity<ApiResponse<SubmitAnswerResponse>> submitAnswer(@PathVariable UUID id, @Valid @RequestBody SubmitAnswerRequest request) {
        SubmitAnswerResponse response = assessmentResponseService.submitAnswer(id, request);
        return ResponseEntity.ok(ApiResponse.success("Answer submitted successfully", response));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<AssessmentResultResponse>> completeAssessment(@PathVariable UUID id) {
        AssessmentResultResponse result = assessmentCompletionService.completeAssessment(id);
        return ResponseEntity.ok(ApiResponse.success("Assessment completed and scored successfully", result));
    }

    @GetMapping("/{id}/result")
    public ResponseEntity<ApiResponse<AssessmentResultResponse>> getAssessmentResult(@PathVariable UUID id) {
        AssessmentResultResponse result = assessmentCompletionService.getAssessmentResult(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
