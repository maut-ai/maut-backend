import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "webhook_subscriptions")
public class WebhookSubscription {

    @Id
    @GeneratedValue(generator = "UUID") // Assumes a UUID generator is configured, or use @GeneratedValue(strategy = GenerationType.UUID) for JPA 3.0+
    private UUID id;

    @Column(name = "client_application_id", nullable = false)
    private UUID clientApplicationId;

    // If you had a ClientApplication entity, you would map it like this:
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "client_application_id", nullable = false)
    // private ClientApplication clientApplication;

    @Column(name = "target_url", nullable = false, length = 2048)
    private String targetUrl;

    @Column(name = "secret", nullable = false, length = 255)
    private String secret; // This should be a securely generated and stored secret

    @Column(name = "event_types", nullable = false, columnDefinition = "TEXT")
    @Convert(converter = StringListConverter.class)
    private List<String> eventTypes;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @CreationTimestamp // Hibernate specific, standard JPA would need @PrePersist
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp // Hibernate specific, standard JPA would need @PreUpdate
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // JPA requires a no-arg constructor
    public WebhookSubscription() {
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getClientApplicationId() {
        return clientApplicationId;
    }

    public void setClientApplicationId(UUID clientApplicationId) {
        this.clientApplicationId = clientApplicationId;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public List<String> getEventTypes() {
        return eventTypes;
    }

    public void setEventTypes(List<String> eventTypes) {
        this.eventTypes = eventTypes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // --- AttributeConverter for List<String> to comma-delimited String ---
    @Converter
    public static class StringListConverter implements AttributeConverter<List<String>, String> {

        private static final String DELIMITER = ",";

        @Override
        public String convertToDatabaseColumn(List<String> attribute) {
            if (attribute == null || attribute.isEmpty()) {
                return "";
            }
            // Ensure no element contains the delimiter itself to avoid issues
            // Or choose a more robust serialization like JSON
            return attribute.stream().map(String::trim).collect(Collectors.joining(DELIMITER));
        }

        @Override
        public List<String> convertToEntityAttribute(String dbData) {
            if (dbData == null || dbData.trim().isEmpty()) {
                return Collections.emptyList();
            }
            return Arrays.stream(dbData.split(DELIMITER))
                         .map(String::trim)
                         .collect(Collectors.toList());
        }
    }

    @Override
    public String toString() {
        return "WebhookSubscription{" +
               "id=" + id +
               ", clientApplicationId=" + clientApplicationId +
               ", targetUrl='" + targetUrl + '\'' +
               ", active=" + active +
               ", eventTypes=" + eventTypes +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               '}';
    }
}
