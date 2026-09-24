package com.jobflow.api.service;

import com.jobflow.api.dto.ApiKeyCreatedResponse;
import com.jobflow.api.dto.ApiKeySummaryResponse;
import com.jobflow.api.exception.ApiKeyNotFoundException;
import com.jobflow.api.security.CurrentUser;
import com.jobflow.common.entity.ApiKey;
import com.jobflow.common.repository.ApiKeyRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class ApiKeyService {

    private static final String KEY_PREFIX = "jf_";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ApiKeyRepository apiKeyRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public ApiKeyService(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    public ApiKeyCreatedResponse createKey(String name) {
        byte[] randomBytes = new byte[32];
        RANDOM.nextBytes(randomBytes);
        String rawKey = KEY_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        ApiKey apiKey = new ApiKey();
        apiKey.setUserId(CurrentUser.get());
        apiKey.setName(name);
        apiKey.setKeyHash(passwordEncoder.encode(rawKey));
        apiKey.setKeyPrefix(rawKey.substring(0, 8));
        apiKey = apiKeyRepository.save(apiKey);

        return new ApiKeyCreatedResponse(apiKey.getId(), apiKey.getName(), rawKey, apiKey.getCreatedAt());
    }

    public List<ApiKeySummaryResponse> listKeys() {
        return apiKeyRepository.findByUserId(CurrentUser.get()).stream()
                .map(k -> new ApiKeySummaryResponse(k.getId(), k.getName(), k.getKeyPrefix(), k.getCreatedAt(), k.getRevokedAt()))
                .toList();
    }

    public void revokeKey(UUID id) {
        ApiKey apiKey = apiKeyRepository.findByIdAndUserId(id, CurrentUser.get())
                .orElseThrow(() -> new ApiKeyNotFoundException(id));
        apiKey.setRevokedAt(Instant.now());
        apiKeyRepository.save(apiKey);
    }
}