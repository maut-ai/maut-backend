package com.maut.core.modules.authenticator.model;

import com.maut.core.modules.user.model.MautUser;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "maut_user_webauthn_credentials", indexes = {
        @Index(name = "idx_maut_user_webauthn_maut_user_id", columnList = "maut_user_id"),
        @Index(name = "idx_maut_user_webauthn_external_id", columnList = "external_id", unique = true)
})
@Data
@NoArgsConstructor
public class MautUserWebauthnCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "maut_user_id", nullable = false)
    private MautUser mautUser;

    @Column(name = "external_id", nullable = false, unique = true, columnDefinition = "TEXT")
    private String externalId; // Base64URL encoded credential ID from authenticator

    @Column(name = "public_key_cose", nullable = false, columnDefinition = "BYTEA")
    private byte[] publicKeyCose; // COSE-encoded public key

    @Column(name = "signature_counter", nullable = false)
    private long signatureCounter;

    @Type(type = "list-array") // Requires hibernate-types dependency or custom UserType for PostgreSQL arrays
    @Column(name = "transports", columnDefinition = "text[]")
    private List<String> transports;

    @Column(name = "friendly_name", columnDefinition = "TEXT")
    private String friendlyName;

    @Column(columnDefinition = "TEXT")
    private String aaguid; // Authenticator Attestation GUID

    @Column(name = "attestation_type", columnDefinition = "TEXT")
    private String attestationType; // e.g., "none", "direct"

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "last_used_at")
    private OffsetDateTime lastUsedAt;
}
