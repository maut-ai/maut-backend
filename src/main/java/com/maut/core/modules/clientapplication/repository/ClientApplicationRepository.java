package com.maut.core.modules.clientapplication.repository;

import com.maut.core.modules.clientapplication.model.ClientApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link ClientApplication} entity.
 */
@Repository
public interface ClientApplicationRepository extends JpaRepository<ClientApplication, UUID> {

    /**
     * Finds a ClientApplication by its Maut API Client ID.
     *
     * @param mautApiClientId The Maut-issued unique identifier for the client API.
     * @return An {@link Optional} containing the found ClientApplication, or empty if not found.
     */
    Optional<ClientApplication> findByMautApiClientId(String mautApiClientId);

}
