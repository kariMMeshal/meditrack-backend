package com.MediTrack.meditrack_backend.Ai_Module;

import com.MediTrack.meditrack_backend.Ai_Module.entity.PredictRequest;
import com.MediTrack.meditrack_backend.Ai_Module.entity.PredictResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Map;

/**
 * Client for the LSTM Imminent Failure Predictor (72-hour window).
 * Calls POST /predict/lstm on the Flask/FastAPI ML service.
 */
@Component
@Slf4j
public class LstmAiClient {

    private final RestClient restClient;
    private final String apiKey;

    public LstmAiClient(
            @Value("${ai.service.url}") String baseUrl,
            @Value("${ai.service.api-key:}") String apiKey
    ) {
        this.apiKey = apiKey;

        // HF Spaces can take 30-60s to wake up from a cold start.
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.defaults()
                .withConnectTimeout(Duration.ofSeconds(10))
                .withReadTimeout(Duration.ofSeconds(60));

        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactoryBuilder
                .detect()
                .build(settings);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @CircuitBreaker(name = "lstmService", fallbackMethod = "predictFallback")
    @Retry(name = "lstmService")
    public PredictResponse predict(PredictRequest request) {
        log.info("Sending LSTM prediction request to ML service");

        return restClient.post()
                .uri("/predict/lstm")
                .headers(this::applyApiKeyIfPresent)
                .body(request)
                .retrieve()
                .body(PredictResponse.class);
    }

    public boolean isModelLoaded() {
        try {
            Map response = restClient.get()
                    .uri("/health")
                    .retrieve()
                    .body(Map.class);

            return response != null && "ok".equals(response.get("status"));
        } catch (Exception ex) {
            log.error("LSTM ML service health probe failed: {}", ex.getMessage());
            return false;
        }
    }

    public PredictResponse predictFallback(PredictRequest request, Throwable ex) {
        log.error("LSTM ML service unavailable, using fallback. Reason: {}", ex.getMessage());
        return null;
    }

    private void applyApiKeyIfPresent(HttpHeaders headers) {
        if (apiKey != null && !apiKey.isBlank()) {
            headers.set("X-API-Key", apiKey);
        }
    }
}