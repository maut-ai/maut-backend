package com.maut.core.modules.clientapplication.model;

import com.maut.core.modules.team.model.Team;
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
        @Index(name = "idx_client_applications_maut_api_client_id", columnList = "maut_api_client_id", unique = true),
        @Index(name = "idx_client_applications_team_id", columnList = "team_id")
})
@Data
@NoArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"}, allowSetters = true)
public class ClientApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    /**
     * The team that owns this client application.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", referencedColumnName = "id", nullable = false)
    private Team team;

    /**
     * Maut-issued unique identifier for the client API. This is public.
     */
    @Column(name = "maut_api_client_id", nullable = false, unique = true)
    private String mautApiClientId;

    /**
     * Human-readable name for the client application.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Stores the plain text client secret used for JWT signing and validation.
     * This secret is essential for the client application to authenticate itself
     * and obtain access tokens.
     */
    @Column(name = "client_secret", nullable = false, length = 255) 
    private String clientSecret; 

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

    // Explicitly adding getter and setter for clientSecret to ensure availability
    public String getClientSecret() {
        return this.clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
}
