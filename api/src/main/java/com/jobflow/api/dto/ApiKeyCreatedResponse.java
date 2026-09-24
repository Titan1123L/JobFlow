package com.jobflow.api.dto;

import java.time.Instant;
import java.util.UUID;

public class ApiKeyCreatedResponse {
    private UUID id;
    private String name;
    private String key;   // raw key - shown once, never again
    private Instant createdAt;

    public ApiKeyCreatedResponse(UUID id, String name, String key, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.key = key;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getKey() { return key; }
    public Instant getCreatedAt() { return createdAt; }
}