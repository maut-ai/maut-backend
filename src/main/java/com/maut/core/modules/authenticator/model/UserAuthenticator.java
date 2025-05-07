package com.maut.core.modules.authenticator.model;

import com.maut.core.modules.user.model.MautUser;
import javax.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents an authenticator (e.g., Passkey) associated with a MautUser.
 */
@Entity
@Table(name = "user_authenticators")
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id") // Performance: only use ID for equals/hashCode
public class UserAuthenticator {

    /**
     * Primary key for the UserAuthenticator.
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    /**
     * The MautUser to whom this authenticator belongs.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "maut_user_id", nullable = false)
    @ToString.Exclude // Avoid circular dependency in toString with MautUser
    private MautUser mautUser;

    /**
     * The type of authenticator.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "authenticator_type", nullable = false)
    private AuthenticatorType authenticatorType;

    /**
     * The ID from Turnkey identifying this specific authenticator (e.g., a passkey credential ID).
     * This should be unique as it's a global identifier from the authenticator provider.
     */
    @Column(name = "turnkey_authenticator_id", unique = true, nullable = false)
    private String turnkeyAuthenticatorId;

    /**
     * The external, globally unique ID of the authenticator (e.g., WebAuthn credential ID/rawId).
     * This is the ID used by the client and the authenticator itself.
     */
    @Column(name = "external_authenticator_id", unique = true, nullable = false)
    private String externalAuthenticatorId;

    /**
     * An optional, user-friendly name for the authenticator (e.g., "Sam's YubiKey", "Phone Passkey").
     */
    @Column(name = "authenticator_name")
    private String authenticatorName;

    /**
     * Indicates whether this authenticator is currently enabled and can be used for authentication.
     */
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true; // Default to true when created

    /**
     * Timestamp of when this UserAuthenticator record was created.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Timestamp of when this UserAuthenticator record was last updated.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
