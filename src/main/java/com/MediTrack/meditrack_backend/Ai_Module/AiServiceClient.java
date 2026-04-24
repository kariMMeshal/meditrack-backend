package com.MediTrack.meditrack_backend.Ai_Module;

import com.MediTrack.meditrack_backend.Ai_Module.entity.PredictRequest;
import com.MediTrack.meditrack_backend.Ai_Module.entity.PredictResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@Slf4j
public class AiServiceClient {

    private final RestClient restClient;
    private final String apiKey;

    public AiServiceClient(
            @Value("${ai.service.url}") String baseUrl,
            @Value("${ai.service.api-key}") String apiKey
    ) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @CircuitBreaker(name = "mlService", fallbackMethod = "predictFallback")
    @Retry(name = "mlService")
    public PredictResponse predict(PredictRequest request) {
        log.info("Sending prediction request to ML service for device sequence");

        return restClient.post()
                .uri("/predict")
                .header("X-API-Key", apiKey)
                .body(request)
                .retrieve()
                .body(PredictResponse.class);
    }

    /**
     * Probes Flask /health to verify ML service is reachable and model is loaded.
     * Returns true if the service responds with status "ok", false otherwise.
     * Does NOT use circuit breaker — health checks must always reach the service.
     */
    public boolean isModelLoaded() {
        try {
            Map response = restClient.get()
                    .uri("/health")
                    .retrieve()
                    .body(Map.class);

            return response != null && "ok".equals(response.get("status"));
        } catch (Exception ex) {
            log.error("ML service health probe failed: {}", ex.getMessage());
            return false;
        }
    }

    // fallback when ML service is down — return null, caller handles gracefully
    public PredictResponse predictFallback(PredictRequest request, Throwable ex) {
        log.error("ML service unavailable, using fallback. Reason: {}", ex.getMessage());
        return null; // caller checks for null and responds accordingly
    }
}