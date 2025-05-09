package com.maut.core.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpServletRequest request) {
        log.warn("No handler found for {}: {}. Message: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                "The requested resource was not found on this server: " + ex.getRequestURL(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("HTTP message not readable for {}: {}. Error: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Request body is missing or malformed. Please ensure the request is well-formed.",
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation failed for request {}: {}. Errors: {}", request.getMethod(), request.getRequestURI(), message);
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validation failed: " + message,
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Illegal argument for request {}: {}. Error: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage() != null ? ex.getMessage() : "Invalid input provided.",
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Consider adding more specific common exceptions like EntityNotFoundException, etc.

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, HttpServletRequest request) {
        HttpStatus statusToReturn = HttpStatus.INTERNAL_SERVER_ERROR;
        String messageToReturn = "An unexpected error occurred. Please try again later.";
        String errorReasonToReturn = HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase();

        if (ex instanceof ResponseStatusException) {
            ResponseStatusException rse = (ResponseStatusException) ex;
            statusToReturn = rse.getStatus();
            messageToReturn = rse.getReason() != null ? rse.getReason() : statusToReturn.getReasonPhrase();
            errorReasonToReturn = statusToReturn.getReasonPhrase();
            // Log ResponseStatusExceptions at WARN level as they are handled exceptions with specific statuses
            log.warn("Handled ResponseStatusException for {}: {} - Status: {}, Reason: '{}', Path: {}", 
                request.getMethod(), request.getRequestURI(), statusToReturn, messageToReturn, request.getRequestURI(), rse);
        } else {
            // Log truly unexpected exceptions at ERROR level
            log.error("Unexpected exception for {}: {}. Path: {}", 
                request.getMethod(), request.getRequestURI(), request.getRequestURI(), ex);
        }

        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                statusToReturn.value(),
                errorReasonToReturn,
                messageToReturn,
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, statusToReturn);
    }
}
