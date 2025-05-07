package com.maut.core.modules.hello.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for HelloMessage.
 * Used for transferring hello message data between the controller and service layers.
 * Part of the hello module within the monolithic application.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HelloMessageDto {
    
    private Long id;
    
    @NotBlank(message = "Message cannot be blank")
    private String message;
    
    private LocalDateTime updatedAt;
}
