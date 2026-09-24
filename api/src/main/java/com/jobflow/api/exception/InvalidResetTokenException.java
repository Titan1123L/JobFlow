package com.jobflow.api.exception;

public class InvalidResetTokenException extends RuntimeException {
    public InvalidResetTokenException() {
        super("This password reset link is invalid, expired, or already used");
    }
}