package com.maut.core.modules.clientapplication.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Represents a client application that integrates with the Maut platform.
 * Each client application has its own unique identifiers and configuration.
 */
@Entity
@Table(name = "client_applications", indexes = {
        @Index(name = "idx_client_applications_maut_api_client_id", columnList = "maut_api_client_id", unique = true)
})
@Data
@NoArgsConstructor
public class ClientApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    /**
     * Maut-issued unique identifier for the client API. This is public.
     */
    @Column(name = "maut_api_client_id", nullable = false, unique = true)
    private String mautApiClientId;

    /**
     * Human-readable name for the client application.
     */
    @Column(nullable = false)
    private String clientName;

    /**
     * Hashed client secret. The actual secret is known only to the client application and Maut (during initial setup).
     * This hash is used to verify the client's identity for certain operations if symmetric key crypto is used.
     */
    @Column(nullable = false, length = 1024) // Length for hash
    private String clientSecretHash;

    /**
     * List of allowed origins (e.g., domains) from which this client application can make requests.
     * Used for CORS and other security checks.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "client_application_allowed_origins", joinColumns = @JoinColumn(name = "client_application_id"))
    @Column(name = "origin")
    private List<String> allowedOrigins;

    /**
     * Flag indicating whether the client application is active and can be used.
     */
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true; // Default to true for new applications

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors, Getters, Setters, etc. will be handled by Lombok's @Data and @NoArgsConstructor
    // Custom business logic methods can be added here if needed.
}
