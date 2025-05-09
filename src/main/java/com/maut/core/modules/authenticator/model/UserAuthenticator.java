package com.maut.core.modules.authenticator.model;

import com.maut.core.modules.user.model.MautUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Represents an authenticator (e.g., Passkey, TOTP device, Security Key) associated with a MautUser.
 */
@Entity
@Table(name = "user_authenticators")
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id") // Performance: only use ID for equals/hashCode
public class UserAuthenticator implements UserDetails {

    /**
     * Primary key for the UserAuthenticator.
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * The MautUser to whom this authenticator belongs.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maut_user_id", nullable = false)
    private MautUser mautUser;

    /**
     * The type of authenticator (e.g., PASSKEY, TOTP).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "authenticator_type", nullable = false)
    private AuthenticatorType authenticatorType;

    /**
     * The external identifier for the authenticator, often provided by a third-party service (e.g., Turnkey Authenticator ID, WebAuthn credential ID in Base64URL).
     * This ID is used to look up the authenticator during assertion/login flows.
     */
    @Column(name = "external_authenticator_id", nullable = false, unique = true)
    private String externalAuthenticatorId;

    /**
     * The ID of the authenticator as known by Turnkey (if applicable).
     */
    @Column(name = "turnkey_authenticator_id", unique = true)
    private String turnkeyAuthenticatorId;

    /**
     * An optional, user-friendly name for the authenticator (e.g., "Sam's YubiKey", "Phone Passkey").
     */
    @Column(name = "authenticator_name")
    private String authenticatorName; // Reverted from authenticatorFriendlyName

    /**
     * Indicates whether this authenticator is currently enabled and can be used for authentication.
     */
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    /**
     * Timestamp of when this authenticator was created.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Timestamp of when this authenticator was last updated.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Timestamp of when the authenticator was last used.
     */
    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    // Relationships & other non-field properties (e.g. UserDetails methods)
    // No explicit getters/setters needed due to @Data

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // For now, return an empty list. This can be expanded if roles/authorities
        // are directly tied to authenticators, which is not typical.
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        // Passkeys don't have a 'password' in the traditional sense for UserDetails
        return null;
    }

    @Override
    public String getUsername() {
        // The 'username' can be the MautUser's identifier or the external authenticator ID
        // Depending on how Spring Security is configured to use this UserDetails object.
        // For passkey-first login, this might be the externalAuthenticatorId initially.
        return this.mautUser != null ? this.mautUser.getMautUserId().toString() : this.externalAuthenticatorId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Accounts don't expire in this context
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Accounts don't get locked via this mechanism
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Credentials (passkeys) don't expire this way
    }

    @Override
    public boolean isEnabled() {
        return this.enabled; // Use the 'enabled' field of the authenticator
    }
}
