package com.financialliteracy.controller;

import com.financialliteracy.dto.common.ApiResponse;
import com.financialliteracy.dto.ml.MlPredictionRequest;
import com.financialliteracy.dto.ml.MlPredictionResponse;
import com.financialliteracy.service.MlPredictionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ml")
@RequiredArgsConstructor
public class MlPredictionController {

    private final MlPredictionService mlPredictionService;

    @GetMapping("/features")
    public ResponseEntity<ApiResponse<List<String>>> getRequiredFeatures() {
        List<String> features = mlPredictionService.getRequiredTop75Features();
        return ResponseEntity.ok(ApiResponse.success("List of required TOP_75 predictor variables", features));
    }

    @PostMapping("/predict")
    public ResponseEntity<ApiResponse<MlPredictionResponse>> predictArchetype(@Valid @RequestBody MlPredictionRequest request) {
        MlPredictionResponse response = mlPredictionService.predictArchetype(request.getFeatures());
        return ResponseEntity.ok(ApiResponse.success("ML Archetype prediction completed successfully", response));
    }

    @PostMapping("/explain")
    public ResponseEntity<ApiResponse<com.financialliteracy.dto.ml.MlExplanationResponse>> explainArchetype(
            @Valid @RequestBody com.financialliteracy.dto.ml.MlExplanationRequest request) {
        com.financialliteracy.dto.ml.MlExplanationResponse response = mlPredictionService.explainArchetype(
                request.getFeatures(),
                request.getTargetClass(),
                request.getTopK()
        );
        return ResponseEntity.ok(ApiResponse.success("TreeSHAP explanation generated successfully", response));
    }
}
