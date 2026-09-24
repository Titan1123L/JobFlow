package com.jobflow.api.exception;

import java.util.UUID;

public class JobNotFoundException extends RuntimeException {
    public JobNotFoundException(UUID id) {
        super("No job exists with id " + id);
    }
}