package com.maut.core.modules.wallet.repository;

import com.maut.core.modules.user.model.MautUser;
import com.maut.core.modules.wallet.model.UserWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link UserWallet} entity.
 */
@Repository
public interface UserWalletRepository extends JpaRepository<UserWallet, UUID> {

    /**
     * Finds a user wallet by its blockchain address.
     *
     * @param walletAddress The blockchain address to search for.
     * @return An {@link Optional} containing the found user wallet, or empty if not found.
     */
    Optional<UserWallet> findByWalletAddress(String walletAddress);

    /**
     * Finds all wallets associated with a specific MautUser.
     *
     * @param mautUser The MautUser whose wallets to retrieve.
     * @return A list of UserWallets belonging to the specified MautUser.
     */
    List<UserWallet> findByMautUser(MautUser mautUser);

    /**
     * Finds a user wallet by its Turnkey Sub-Organization ID.
     *
     * @param turnkeySubOrganizationId The Turnkey Sub-Organization ID.
     * @return An {@link Optional} of UserWallet.
     */
    Optional<UserWallet> findByTurnkeySubOrganizationId(String turnkeySubOrganizationId);

    /**
     * Checks if a UserWallet exists for the given MautUser.
     *
     * @param mautUser The MautUser to check for.
     * @return true if a wallet exists for the user, false otherwise.
     */
    boolean existsByMautUser(MautUser mautUser);

}
