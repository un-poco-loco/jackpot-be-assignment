package com.jackpot.controller;

import com.jackpot.exception.BetNotFoundException;
import com.jackpot.exception.InvalidBetIdException;
import com.jackpot.exception.JackpotNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST controllers.
 * Handles validation errors and provides user-friendly error messages.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles validation errors from @Valid annotations.
     * Returns a 400 Bad Request with detailed field-level error messages.
     *
     * @param ex the validation exception
     * @return response entity with validation error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("error", "Validation failed");
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("details", fieldErrors);

        logger.warn("Validation error: {}", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles bet not found exceptions.
     * Returns a 404 Not Found with descriptive error message.
     *
     * @param ex the bet not found exception
     * @return response entity with error details
     */
    @ExceptionHandler(BetNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleBetNotFoundException(BetNotFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Bet not found");
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("message", ex.getMessage());

        logger.warn("Bet not found: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handles jackpot not found exceptions.
     * Returns a 404 Not Found with descriptive error message.
     *
     * @param ex the jackpot not found exception
     * @return response entity with error details
     */
    @ExceptionHandler(JackpotNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleJackpotNotFoundException(JackpotNotFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Jackpot not found");
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("message", ex.getMessage());

        logger.warn("Jackpot not found: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handles invalid bet ID exceptions.
     * Returns a 400 Bad Request with descriptive error message.
     *
     * @param ex the invalid bet ID exception
     * @return response entity with error details
     */
    @ExceptionHandler(InvalidBetIdException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidBetIdException(InvalidBetIdException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Invalid bet ID");
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("message", ex.getMessage());

        logger.warn("Invalid bet ID: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles type mismatch exceptions (e.g., passing string instead of number).
     * Returns a 400 Bad Request with descriptive error message.
     *
     * @param ex the type mismatch exception
     * @return response entity with error details
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Invalid parameter type");
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("message", String.format("Parameter '%s' must be a valid %s",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "type"));

        logger.warn("Type mismatch error: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles generic runtime exceptions.
     * Returns a 500 Internal Server Error with descriptive error message.
     *
     * @param ex the runtime exception
     * @return response entity with error details
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Internal server error");
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("message", ex.getMessage());

        logger.error("Runtime exception occurred: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
