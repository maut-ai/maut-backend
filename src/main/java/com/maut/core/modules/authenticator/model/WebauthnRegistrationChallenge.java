package com.maut.core.modules.authenticator.model;

import com.maut.core.modules.user.model.MautUser;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "webauthn_registration_challenges", indexes = {
        @Index(name = "idx_webauthn_reg_challenges_maut_user_id", columnList = "maut_user_id"),
        @Index(name = "idx_webauthn_reg_challenges_expires_at", columnList = "expires_at")
})
@Data
@NoArgsConstructor
public class WebauthnRegistrationChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "maut_user_id", nullable = false)
    private MautUser mautUser;

    @Column(nullable = false, unique = true, columnDefinition = "TEXT") // Challenges should be unique while active
    private String challenge;

    @Column(name = "relying_party_id", nullable = false, columnDefinition = "TEXT")
    private String relyingPartyId;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public WebauthnRegistrationChallenge(MautUser mautUser, String challenge, String relyingPartyId, OffsetDateTime expiresAt) {
        this.mautUser = mautUser;
        this.challenge = challenge;
        this.relyingPartyId = relyingPartyId;
        this.expiresAt = expiresAt;
    }
}
