package com.maut.core.modules.wallet.service;

import com.maut.core.modules.user.model.MautUser;
import com.maut.core.modules.wallet.dto.EnrollWalletResponse;
import com.maut.core.modules.wallet.dto.WalletDetailsResponse;

public interface WalletService {

    /**
     * Enrolls a new wallet for the given MautUser.
     * This process involves creating a Turnkey sub-organization, generating a private key,
     * and associating the new wallet with the user.
     *
     * @param mautUser The MautUser for whom the wallet is being enrolled. Must not be null.
     * @param walletDisplayName An optional display name for the wallet.
     * @return EnrollWalletResponse containing the new wallet's address.
     * @throws com.maut.core.common.exception.UserAlreadyHasWalletException if the user already has a wallet.
     * @throws com.maut.core.common.exception.TurnkeyOperationException if an error occurs during Turnkey operations.
     */
    EnrollWalletResponse enrollNewWallet(MautUser mautUser, String walletDisplayName);

    /**
     * Retrieves the details of the MautUser's wallet.
     *
     * @param mautUser The MautUser whose wallet details are being requested. Must not be null.
     * @return WalletDetailsResponse containing the wallet's information.
     * @throws com.maut.core.common.exception.ResourceNotFoundException if the user does not have a wallet.
     */
    WalletDetailsResponse getWalletDetails(MautUser mautUser);
}
