package com.jobflow.api.exception;

public class JobResultNotAvailableException extends RuntimeException {
    public JobResultNotAvailableException(String message) {
        super(message);
    }
}