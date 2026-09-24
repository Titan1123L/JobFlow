package com.jobflow.api.dto;

import java.time.Instant;
import java.util.UUID;

public class ApiKeySummaryResponse {
    private UUID id;
    private String name;
    private String keyPrefix;
    private Instant createdAt;
    private Instant revokedAt;

    public ApiKeySummaryResponse(UUID id, String name, String keyPrefix, Instant createdAt, Instant revokedAt) {
        this.id = id;
        this.name = name;
        this.keyPrefix = keyPrefix;
        this.createdAt = createdAt;
        this.revokedAt = revokedAt;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getKeyPrefix() { return keyPrefix; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getRevokedAt() { return revokedAt; }
}