package com.maut.core.modules.wallet.model;

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
 * Represents a user's MPC wallet within the Maut system.
 */
@Entity
@Table(name = "user_wallets")
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id") // Performance: only use ID for equals/hashCode
public class UserWallet {

    /**
     * Primary key for the UserWallet.
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    /**
     * The MautUser to whom this wallet belongs.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "maut_user_id", nullable = false)
    @ToString.Exclude // Avoid circular dependency in toString with MautUser
    private MautUser mautUser;

    /**
     * The blockchain address of the wallet.
     * This should be unique across all wallets.
     */
    @Column(name = "wallet_address", unique = true, nullable = false)
    private String walletAddress;

    /**
     * An optional, user-friendly display name for the wallet (e.g., "My Main Wallet").
     */
    @Column(name = "wallet_display_name")
    private String walletDisplayName;

    /**
     * The ID from Turnkey identifying the sub-organization associated with this wallet.
     * This should be unique as each wallet corresponds to a Turnkey sub-organization.
     */
    @Column(name = "turnkey_sub_organization_id", unique = true, nullable = false)
    private String turnkeySubOrganizationId;

    /**
     * The ID from Turnkey for the Maut-held private key share associated with this wallet.
     * This should be unique.
     */
    @Column(name = "turnkey_maut_private_key_id", unique = true, nullable = false)
    private String turnkeyMautPrivateKeyId; 
    // Renamed from turnkeyPrivateKeyId to be more specific based on v0_project_spec.md Section 3.3

    /**
     * The ID from Turnkey for the user-held private key share associated with this wallet.
     * This should be unique.
     */
    @Column(name = "turnkey_user_private_key_id", unique = true, nullable = false)
    private String turnkeyUserPrivateKeyId; // Added based on v0_project_spec.md Section 3.3

    /**
     * The ID of the default Turnkey Policy to be used for signing transactions with this wallet.
     * This is optional and can be overridden per transaction.
     */
    @Column(name = "default_turnkey_policy_id")
    private String defaultTurnkeyPolicyId; // Renamed from defaultPolicyId for clarity

    /**
     * Timestamp of when this UserWallet record was created.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Timestamp of when this UserWallet record was last updated.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}
