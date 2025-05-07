package com.maut.core.common.exception.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for standardized error responses across all modules.
 * This ensures consistent error response format throughout the entire application.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDto {
    
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    
    // Getters and setters
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public int getStatus() {
        return status;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    /**
     * Default builder that sets the timestamp to now.
     */
    public static ErrorResponseDtoBuilder builder() {
        return new ErrorResponseDtoBuilder().timestamp(LocalDateTime.now());
    }
    
    /**
     * Static builder class for ErrorResponseDto
     */
    public static class ErrorResponseDtoBuilder {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
        
        public ErrorResponseDtoBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public ErrorResponseDtoBuilder status(int status) {
            this.status = status;
            return this;
        }
        
        public ErrorResponseDtoBuilder error(String error) {
            this.error = error;
            return this;
        }
        
        public ErrorResponseDtoBuilder message(String message) {
            this.message = message;
            return this;
        }
        
        public ErrorResponseDtoBuilder path(String path) {
            this.path = path;
            return this;
        }
        
        public ErrorResponseDto build() {
            ErrorResponseDto dto = new ErrorResponseDto();
            dto.setTimestamp(this.timestamp);
            dto.setStatus(this.status);
            dto.setError(this.error);
            dto.setMessage(this.message);
            dto.setPath(this.path);
            return dto;
        }
    }
}
