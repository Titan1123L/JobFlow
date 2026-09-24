package com.jobflow.api.controller;

import com.jobflow.api.dto.ApiKeyCreatedResponse;
import com.jobflow.api.dto.ApiKeySummaryResponse;
import com.jobflow.api.dto.CreateApiKeyRequest;
import com.jobflow.api.service.ApiKeyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/keys")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @PostMapping
    public ResponseEntity<ApiKeyCreatedResponse> createKey(@Valid @RequestBody CreateApiKeyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(apiKeyService.createKey(request.getName()));
    }

    @GetMapping
    public ResponseEntity<List<ApiKeySummaryResponse>> listKeys() {
        return ResponseEntity.ok(apiKeyService.listKeys());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revokeKey(@PathVariable UUID id) {
        apiKeyService.revokeKey(id);
        return ResponseEntity.ok().build();
    }
}