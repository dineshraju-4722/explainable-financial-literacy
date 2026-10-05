package com.financialliteracy.ml;

import com.financialliteracy.dto.ml.MlPredictionRequest;
import com.financialliteracy.dto.ml.MlPredictionResponse;
import com.financialliteracy.exception.MlServiceException;
import com.financialliteracy.exception.MlServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class MlPredictionClientImpl implements MlPredictionClient {

    private final RestTemplate restTemplate;
    private final MlServiceProperties properties;

    public MlPredictionClientImpl(
            @Qualifier("mlRestTemplate") RestTemplate restTemplate,
            MlServiceProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    @Override
    public MlPredictionResponse predict(MlPredictionRequest request) {
        String targetUrl = properties.getUrl() + "/predict";
        log.info("Sending ML prediction request to FastAPI service at: {}", targetUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<MlPredictionRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<MlPredictionResponse> responseEntity = restTemplate.postForEntity(
                    targetUrl, entity, MlPredictionResponse.class);

            MlPredictionResponse response = responseEntity.getBody();

            if (response == null || response.getArchetype() == null || response.getProbabilities() == null) {
                log.error("Received malformed or null prediction response from ML service at {}", targetUrl);
                throw new MlServiceException("Received malformed or null response from ML prediction service");
            }

            log.info("Successfully received prediction from ML service: Archetype={}, Version={}",
                    response.getArchetype(), response.getModelVersion());

            return response;

        } catch (ResourceAccessException e) {
            log.error("Failed to connect or timeout calling ML service at {}: {}", targetUrl, e.getMessage());
            throw new MlServiceUnavailableException("ML Prediction service is unreachable at " + targetUrl, e);
        } catch (HttpStatusCodeException e) {
            log.error("ML service returned HTTP {} error: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new MlServiceException("ML prediction service returned HTTP error " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("Unexpected error calling ML service at {}: {}", targetUrl, e.getMessage());
            throw new MlServiceException("Unexpected failure communicating with ML prediction service: " + e.getMessage(), e);
        }
    }

    @Override
    public com.financialliteracy.dto.ml.MlExplanationResponse explain(com.financialliteracy.dto.ml.MlExplanationRequest request) {
        String targetUrl = properties.getUrl() + "/explain";
        log.info("Sending TreeSHAP explanation request to FastAPI service at: {}", targetUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<com.financialliteracy.dto.ml.MlExplanationRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<com.financialliteracy.dto.ml.MlExplanationResponse> responseEntity = restTemplate.postForEntity(
                    targetUrl, entity, com.financialliteracy.dto.ml.MlExplanationResponse.class);

            com.financialliteracy.dto.ml.MlExplanationResponse response = responseEntity.getBody();

            if (response == null || response.getArchetype() == null || response.getExplanation() == null) {
                log.error("Received malformed or null explanation response from ML service at {}", targetUrl);
                throw new MlServiceException("Received malformed or null response from ML explanation service");
            }

            log.info("Successfully received TreeSHAP explanation from ML service: Archetype={}, TargetClass={}",
                    response.getArchetype(), response.getExplanation().getTargetClass());

            return response;

        } catch (ResourceAccessException e) {
            log.error("Failed to connect or timeout calling ML service at {}: {}", targetUrl, e.getMessage());
            throw new MlServiceUnavailableException("ML Explanation service is unreachable at " + targetUrl, e);
        } catch (HttpStatusCodeException e) {
            log.error("ML service returned HTTP {} error: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new MlServiceException("ML explanation service returned HTTP error " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("Unexpected error calling ML explanation service at {}: {}", targetUrl, e.getMessage(), e);
            throw new MlServiceException("Unexpected failure communicating with ML explanation service: " + e.getMessage(), e);
        }
    }
}
