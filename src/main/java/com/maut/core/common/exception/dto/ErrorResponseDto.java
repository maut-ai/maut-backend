package com.maut.core.common.exception.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for standardized error responses across all modules.
 * This ensures consistent error response format throughout the entire application.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDto {
    
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    
    /**
     * Default builder that sets the timestamp to now.
     */
    public static ErrorResponseDto.ErrorResponseDtoBuilder builder() {
        return new ErrorResponseDtoBuilder().timestamp(LocalDateTime.now());
    }
}
