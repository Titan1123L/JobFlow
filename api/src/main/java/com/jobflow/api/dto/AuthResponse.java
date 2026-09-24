package com.jobflow.api.dto;

import java.time.Instant;

public class AuthResponse {
    private String token;
    private Instant expiresAt;

    public AuthResponse(String token, Instant expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public String getToken() { return token; }
    public Instant getExpiresAt() { return expiresAt; }
}