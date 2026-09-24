package com.jobflow.api.exception;

import java.util.UUID;

public class ApiKeyNotFoundException extends RuntimeException {
    public ApiKeyNotFoundException(UUID id) {
        super("No API key exists with id " + id);
    }
}