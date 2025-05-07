package com.maut.core.modules.user.repository;

import com.maut.core.modules.clientapplication.model.ClientApplication; // Updated import
import com.maut.core.modules.user.model.MautUser; // Updated import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link MautUser} entity.
 */
@Repository
public interface MautUserRepository extends JpaRepository<MautUser, UUID> {

    /**
     * Finds a MautUser by their Maut User ID.
     *
     * @param mautUserId The Maut-specific unique user identifier.
     * @return An {@link Optional} containing the found MautUser, or empty if not found.
     */
    Optional<MautUser> findByMautUserId(UUID mautUserId);

    /**
     * Finds a MautUser by their client application and the user's ID within that client's system.
     *
     * @param clientApplication The client application.
     * @param clientSystemUserId The user's ID within the client application's system.
     * @return An {@link Optional} containing the found MautUser, or empty if not found.
     */
    Optional<MautUser> findByClientApplicationAndClientSystemUserId(ClientApplication clientApplication, String clientSystemUserId);

}
