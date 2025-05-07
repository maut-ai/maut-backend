package com.maut.core.modules.hello.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity class representing a hello message stored in the database.
 * This is part of the hello module within the larger monolithic application.
 */
@Entity
@Table(name = "hello_message")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HelloMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Explicit getter/setter methods to avoid Lombok processing issues
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

    /**
     * Pre-persist hook to set the updatedAt timestamp before saving.
     */
    @PrePersist
    @PreUpdate
    public void prePersist() {
        updatedAt = LocalDateTime.now();
    }
}
