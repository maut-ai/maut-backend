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
    
    // Explicit getter and setter methods
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Explicit static builder method
    public static HelloMessageDtoBuilder builder() {
        return new HelloMessageDtoBuilder();
    }
    
    // Builder class implementation
    public static class HelloMessageDtoBuilder {
        private Long id;
        private String message;
        private LocalDateTime updatedAt;
        
        public HelloMessageDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }
        
        public HelloMessageDtoBuilder message(String message) {
            this.message = message;
            return this;
        }
        
        public HelloMessageDtoBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }
        
        public HelloMessageDto build() {
            HelloMessageDto dto = new HelloMessageDto();
            dto.setId(this.id);
            dto.setMessage(this.message);
            dto.setUpdatedAt(this.updatedAt);
            return dto;
        }
    }
}
