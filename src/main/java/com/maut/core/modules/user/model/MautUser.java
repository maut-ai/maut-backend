package com.maut.core.modules.user.model;

import com.maut.core.modules.clientapplication.model.ClientApplication; // Updated import
import com.maut.core.modules.wallet.model.UserWallet;
import com.maut.core.modules.authenticator.model.UserAuthenticator;
import com.maut.core.modules.team.model.Team; // Added import for Team
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents an end-user within a client application's ecosystem, managed by Maut.
 * A MautUser is unique within Maut, identified by a `mautUserId` (UUID), and is associated with a specific
 * `ClientApplication` via `client_application_id` and the user's identifier within that client's system (`clientSystemUserId`).
 * MautUsers typically authenticate using mechanisms like passkeys and authorize API requests using a `mautSessionToken`.
 * This is distinct from `User`, which represents accounts for accessing Maut's own dashboards.
 */
@Entity
@Table(name = "maut_users", indexes = {
        @Index(name = "idx_maut_users_maut_user_id", columnList = "maut_user_id", unique = true),
        @Index(name = "idx_maut_users_client_app_client_system_user_id", columnList = "client_application_id, client_system_user_id", unique = true)
})
@Data
@NoArgsConstructor
public class MautUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id; // Internal DB primary key

    /**
     * Maut's globally unique identifier for the user.
     */
    @Column(name = "maut_user_id", nullable = false, unique = true, columnDefinition = "UUID")
    private UUID mautUserId;

    /**
     * The client application this user belongs to or was registered through.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_application_id", nullable = false)
    private ClientApplication clientApplication;

    /**
     * The team this user belongs to, derived from the client application.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = true) // Nullable for now, can be made non-nullable if always set
    private Team team;

    /**
     * The user's identifier within the client application's system.
     * This, in combination with clientApplication, uniquely identifies the user from the client's perspective.
     */
    @Column(name = "client_system_user_id", nullable = false)
    private String clientSystemUserId;

    @OneToMany(mappedBy = "mautUser", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<UserWallet> userWallets = new ArrayList<>();

    @OneToMany(mappedBy = "mautUser", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<UserAuthenticator> userAuthenticators = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Helper methods for bidirectional relationships can be added here if needed
    // e.g., addUserWallet, removeUserWallet, addUserAuthenticator, removeUserAuthenticator
}
