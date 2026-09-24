package com.jobflow.api.security;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobflow.common.entity.ApiKey;
import com.jobflow.common.repository.ApiKeyRepository;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiKeyAuthFilter implements Filter {

    private final RateLimiter rateLimiter;
    private final JwtService jwtService;
    private final ApiKeyRepository apiKeyRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ApiKeyAuthFilter(RateLimiter rateLimiter, JwtService jwtService, ApiKeyRepository apiKeyRepository) {
        this.rateLimiter = rateLimiter;
        this.jwtService = jwtService;
        this.apiKeyRepository = apiKeyRepository;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

            if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String path = httpRequest.getRequestURI();
        if (path.startsWith("/api/auth/")) {
            chain.doFilter(request, response);
            return;
        }

        UUID userId = null;

        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                userId = jwtService.validateAndGetUserId(authHeader.substring(7));
            } catch (Exception ex) {
                writeError(httpResponse, 401, "UNAUTHORIZED", "Invalid or expired token");
                return;
            }
        } else {
            String providedKey = httpRequest.getHeader("X-API-Key");
            if (providedKey != null) {
                userId = resolveApiKey(providedKey);
                if (userId == null) {
                    writeError(httpResponse, 401, "UNAUTHORIZED", "Invalid or revoked API key");
                    return;
                }
            }
        }

        if (userId == null) {
            writeError(httpResponse, 401, "UNAUTHORIZED", "Missing or invalid authentication");
            return;
        }

        try {
            CurrentUser.set(userId);

            boolean isJobSubmission = "POST".equalsIgnoreCase(httpRequest.getMethod())
                    && "/api/jobs".equals(httpRequest.getRequestURI());

            if (isJobSubmission && !rateLimiter.tryConsume(userId.toString())) {
                writeError(httpResponse, 429, "RATE_LIMIT_EXCEEDED", "Rate limit exceeded for this account");
                return;
            }

            chain.doFilter(request, response);
        } finally {
            CurrentUser.clear();
        }
    }

    /** BCrypt hashes can't be looked up by value directly, so we check the provided key against every stored hash. */
    private UUID resolveApiKey(String providedKey) {
        List<ApiKey> allKeys = apiKeyRepository.findAll();
        for (ApiKey apiKey : allKeys) {
            if (apiKey.getRevokedAt() == null && passwordEncoder.matches(providedKey, apiKey.getKeyHash())) {
                return apiKey.getUserId();
            }
        }
        return null;
    }

        private void writeError(HttpServletResponse response, int status, String code, String message) throws IOException {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setStatus(status);
        response.setContentType("application/json");
        Map<String, Object> body = new HashMap<>();
        body.put("code", code);
        body.put("message", message);
        body.put("details", null);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
