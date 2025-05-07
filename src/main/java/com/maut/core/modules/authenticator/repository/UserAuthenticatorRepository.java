package com.maut.core.modules.authenticator.repository;

import com.maut.core.modules.user.model.MautUser;
import com.maut.core.modules.authenticator.model.UserAuthenticator;
import com.maut.core.modules.authenticator.model.AuthenticatorType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link UserAuthenticator} entity.
 */
@Repository
public interface UserAuthenticatorRepository extends JpaRepository<UserAuthenticator, UUID> {

    /**
     * Finds all authenticators associated with a specific MautUser, with pagination.
     *
     * @param mautUser The MautUser whose authenticators to retrieve.
     * @param pageable Pagination information.
     * @return A page of UserAuthenticators belonging to the specified MautUser.
     */
    Page<UserAuthenticator> findByMautUser(MautUser mautUser, Pageable pageable);

    /**
     * Finds all authenticators of a specific type for a given MautUser.
     *
     * @param mautUser The MautUser.
     * @param authenticatorType The type of authenticator.
     * @return A list of UserAuthenticators matching the criteria.
     */
    List<UserAuthenticator> findByMautUserAndAuthenticatorType(MautUser mautUser, AuthenticatorType authenticatorType);

    /**
     * Finds an authenticator by its Turnkey Authenticator ID.
     *
     * @param turnkeyAuthenticatorId The Turnkey-specific ID for the authenticator.
     * @return An {@link Optional} containing the found UserAuthenticator, or empty if not found.
     */
    Optional<UserAuthenticator> findByTurnkeyAuthenticatorId(String turnkeyAuthenticatorId);

}
