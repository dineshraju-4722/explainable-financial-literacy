package com.financialliteracy.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final DataSource dataSource;

    @Value("${app.ml-service.url:http://localhost:8000}")
    private String mlServiceUrl;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "backend");
        response.put("version", "1.0.0");
        response.put("timestamp", Instant.now().toString());

        // Check PostgreSQL connection status
        Map<String, Object> dbHealth = new HashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            dbHealth.put("status", "UP");
            dbHealth.put("database", conn.getCatalog());
        } catch (Exception e) {
            dbHealth.put("status", "DOWN");
            dbHealth.put("error", e.getMessage());
        }
        response.put("database", dbHealth);

        // Check Python ML Service connectivity
        Map<String, Object> mlHealth = new HashMap<>();
        try {
            RestTemplate restTemplate = new RestTemplate();
            String healthUrl = mlServiceUrl + "/health";
            ResponseEntity<Map> mlResponse = restTemplate.getForEntity(healthUrl, Map.class);
            mlHealth.put("status", mlResponse.getStatusCode().is2xxSuccessful() ? "UP" : "DOWN");
            mlHealth.put("mlServiceUrl", mlServiceUrl);
            mlHealth.put("details", mlResponse.getBody());
        } catch (Exception e) {
            mlHealth.put("status", "DOWN");
            mlHealth.put("mlServiceUrl", mlServiceUrl);
            mlHealth.put("error", e.getMessage());
        }
        response.put("mlService", mlHealth);

        return ResponseEntity.ok(response);
    }
}
