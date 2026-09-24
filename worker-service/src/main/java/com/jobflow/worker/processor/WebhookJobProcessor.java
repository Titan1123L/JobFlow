package com.jobflow.worker.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobflow.common.entity.Job;
import com.jobflow.common.security.SsrfGuard;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Component
public class WebhookJobProcessor implements JobProcessor {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @SuppressWarnings("unchecked")
    public void process(Job job) throws Exception {
        Map<String, Object> payload = job.getPayload();
        String url = (String) payload.get("url");
        String method = ((String) payload.get("method")).toUpperCase();
        Map<String, Object> headers = (Map<String, Object>) payload.getOrDefault("headers", Map.of());
        Object body = payload.get("body");

        // Re-check immediately before dispatch, not just at submission time -
        // closes the DNS-rebinding gap where a hostname could resolve to a
        // private IP by now even if it was public when the job was submitted
        SsrfGuard.assertSafe(url);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15));

        for (Map.Entry<String, Object> header : headers.entrySet()) {
            requestBuilder.header(header.getKey(), String.valueOf(header.getValue()));
        }

        String bodyJson = "";
        if (body != null) {
            bodyJson = objectMapper.writeValueAsString(body);
            requestBuilder.header("Content-Type", "application/json");
        }

        switch (method) {
            case "GET" -> requestBuilder.GET();
            case "POST" -> requestBuilder.POST(HttpRequest.BodyPublishers.ofString(bodyJson));
            case "PUT" -> requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(bodyJson));
            default -> throw new IllegalArgumentException("Unsupported method: " + method);
        }

        HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            // Non-2xx counts as failure - this feeds straight into the existing retry/backoff/DLQ logic
            throw new RuntimeException("Webhook call failed with status " + response.statusCode() + ": " + response.body());
        }
    }
}