package com.jobflow.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

        @ExceptionHandler(JobNotFoundException.class)
        public ResponseEntity<Map<String, Object>> handleNotFound(JobNotFoundException ex) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(errorBody("NOT_FOUND", ex.getMessage(), null));
        }

        @ExceptionHandler({ org.springframework.amqp.AmqpException.class,
                        org.springframework.dao.DataAccessException.class })
        public ResponseEntity<Map<String, Object>> handleUnavailable(Exception ex) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body(errorBody("SERVICE_UNAVAILABLE",
                                                "A required dependency is temporarily unreachable", null));
        }

        @ExceptionHandler(ValidationException.class)
        public ResponseEntity<Map<String, Object>> handleValidation(ValidationException ex) {
                Map<String, Object> details = new HashMap<>();
                details.put("field", ex.getField());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(errorBody("VALIDATION_ERROR", ex.getMessage(), details));
        }

        @ExceptionHandler(InvalidStateException.class)
        public ResponseEntity<Map<String, Object>> handleInvalidState(InvalidStateException ex) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(errorBody("CONFLICT", ex.getMessage(), null));
        }

        @ExceptionHandler(EmailAlreadyExistsException.class)
        public ResponseEntity<Map<String, Object>> handleEmailExists(EmailAlreadyExistsException ex) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(errorBody("CONFLICT", ex.getMessage(), null));
        }

        @ExceptionHandler(InvalidCredentialsException.class)
        public ResponseEntity<Map<String, Object>> handleInvalidCredentials(InvalidCredentialsException ex) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(errorBody("UNAUTHORIZED", ex.getMessage(), null));
        }

        @ExceptionHandler(ApiKeyNotFoundException.class)
        public ResponseEntity<Map<String, Object>> handleApiKeyNotFound(ApiKeyNotFoundException ex) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(errorBody("NOT_FOUND", ex.getMessage(), null));
        }

        @ExceptionHandler(JobResultNotAvailableException.class)
        public ResponseEntity<Map<String, Object>> handleResultNotAvailable(JobResultNotAvailableException ex) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(errorBody("CONFLICT", ex.getMessage(), null));
        }

        @ExceptionHandler(InvalidResetTokenException.class)
        public ResponseEntity<Map<String, Object>> handleInvalidResetToken(InvalidResetTokenException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(errorBody("VALIDATION_ERROR", ex.getMessage(), null));
        }

        // Triggered automatically when a @Valid DTO fails its annotations (e.g. jobType
        // missing)
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, Object>> handleBeanValidation(MethodArgumentNotValidException ex) {
                String field = ex.getBindingResult().getFieldErrors().isEmpty()
                                ? "unknown"
                                : ex.getBindingResult().getFieldErrors().get(0).getField();
                String message = ex.getBindingResult().getFieldErrors().isEmpty()
                                ? "Validation failed"
                                : ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
                Map<String, Object> details = new HashMap<>();
                details.put("field", field);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(errorBody("VALIDATION_ERROR", message, details));
        }

        private Map<String, Object> errorBody(String code, String message, Object details) {
                Map<String, Object> body = new HashMap<>();
                body.put("code", code);
                body.put("message", message);
                body.put("details", details);
                return body;
        }
}