package com.wonwire.wonwire.exception;

import com.wonwire.wonwire.dto.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Global exception handler that catches all exceptions thrown across the application
 * and returns consistent, clean HTTP error responses in JSON format.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles user already exists errors.
     * Returns 409 Conflict.
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserAlreadyExists(
            UserAlreadyExistsException ex) {
        return buildError(HttpStatus.CONFLICT, "Conflict", ex.getMessage());
    }

    /**
     * Handles user not found errors.
     * Returns 404 Not Found.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotFound(
            UserNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }

    /**
     * Handles insufficient balance errors during transfers.
     * Returns 422 Unprocessable Entity.
     */
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponseDTO> handleInsufficientBalance(
            InsufficientBalanceException ex) {
        return buildError(HttpStatus.UNPROCESSABLE_ENTITY,
                "Unprocessable Entity", ex.getMessage());
    }

    /**
     * Handles invalid transfer errors.
     * Returns 400 Bad Request.
     */
    @ExceptionHandler(InvalidTransferException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidTransfer(
            InvalidTransferException ex) {
        return buildError(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }

    /**
     * Handles invalid credentials during login.
     * Returns 401 Unauthorized.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadCredentials(
            BadCredentialsException ex) {
        return buildError(HttpStatus.UNAUTHORIZED,
                "Unauthorized", "Invalid email or password");
    }

    /**
     * Handles @Valid validation failures on request DTOs.
     * Collects all field errors and returns them in a single 400 response.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return buildError(HttpStatus.BAD_REQUEST, "Validation Failed", message);
    }

    /**
     * Catches all unhandled exceptions (no Java stack trace should be shown to the frontend)
     * Returns 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error", "An unexpected error occurred");
    }

    /**
     * Private method to prevent useless code duplication.
     * All errors have the same JSON format
     */
    private ResponseEntity<ErrorResponseDTO> buildError(
            HttpStatus status, String error, String message) {
        return ResponseEntity
                .status(status)
                .body(ErrorResponseDTO.builder()
                        .status(status.value())
                        .error(error)
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .build());
    }
}