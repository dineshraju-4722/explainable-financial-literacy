package com.financialliteracy.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class IncompleteFeatureVectorException extends RuntimeException {

    private final List<String> missingFeatures;

    public IncompleteFeatureVectorException(List<String> missingFeatures) {
        super("Incomplete TOP_75 feature vector. Missing " + missingFeatures.size() + " required predictor variables: " + missingFeatures);
        this.missingFeatures = missingFeatures;
    }
}
