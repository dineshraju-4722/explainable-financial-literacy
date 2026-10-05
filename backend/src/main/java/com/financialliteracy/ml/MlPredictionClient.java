package com.financialliteracy.ml;

import com.financialliteracy.dto.ml.MlExplanationRequest;
import com.financialliteracy.dto.ml.MlExplanationResponse;
import com.financialliteracy.dto.ml.MlPredictionRequest;
import com.financialliteracy.dto.ml.MlPredictionResponse;

public interface MlPredictionClient {
    MlPredictionResponse predict(MlPredictionRequest request);

    MlExplanationResponse explain(MlExplanationRequest request);
}
