package com.MediTrack.meditrack_backend.Ai_Module;

import com.MediTrack.meditrack_backend.Ai_Module.dto.RfFlaskRequest;
import com.MediTrack.meditrack_backend.Ai_Module.dto.RfFlaskResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class RfAiClient {

    private final RestClient restClient;
    private final String apiKey;

    public RfAiClient(
            @Value("${ai.service.url}") String baseUrl,
            @Value("${ai.service.api-key}") String apiKey
    ) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @CircuitBreaker(name = "mlService", fallbackMethod = "predictRfFallback")
    @Retry(name = "mlService")
    public RfFlaskResponse predict(RfFlaskRequest request) {
        log.info("Sending RF risk assessment request to ML service");

        return restClient.post()
                .uri("/predict/rf")
                .header("X-API-Key", apiKey)
                .body(request)
                .retrieve()
                .body(RfFlaskResponse.class);
    }

    // fallback when ML service is down — caller handles null gracefully
    public RfFlaskResponse predictRfFallback(RfFlaskRequest request, Throwable ex) {
        log.error("RF ML service unavailable, using fallback. Reason: {}", ex.getMessage());
        return null;
    }
}