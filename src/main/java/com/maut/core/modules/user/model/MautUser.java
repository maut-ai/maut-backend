package com.maut.core.modules.user.model;

import com.maut.core.modules.clientapplication.model.ClientApplication; // Updated import
import com.maut.core.modules.wallet.model.UserWallet;
import com.maut.core.modules.authenticator.model.UserAuthenticator;
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
 * Represents a Maut user. A MautUser is unique within the Maut ecosystem and is identified by a mautUserId.
 * A MautUser is associated with a specific ClientApplication and has a clientSystemUserId which is the user's ID
 * within that client's system.
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
