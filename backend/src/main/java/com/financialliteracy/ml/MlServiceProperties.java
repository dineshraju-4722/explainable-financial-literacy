package com.financialliteracy.ml;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.ml-service")
public class MlServiceProperties {

    /**
     * FastAPI ML Microservice Base URL.
     * Default local: http://localhost:8000
     * Docker: http://ml-service:8000
     */
    private String url = "http://localhost:8000";

    /**
     * HTTP Connection Timeout in milliseconds.
     */
    private int connectTimeoutMs = 3000;

    /**
     * HTTP Read Timeout in milliseconds.
     */
    private int readTimeoutMs = 5000;
}
