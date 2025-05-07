package com.maut.core.modules.transaction.service;

import com.maut.core.modules.transaction.dto.InitiateSigningRequest;
import com.maut.core.modules.transaction.dto.InitiateSigningResponse;
import com.maut.core.modules.user.model.MautUser;

public interface TransactionService {

    /**
     * Initiates a transaction signing process with Turnkey.
     *
     * @param mautUser The MautUser initiating the transaction. Must not be null.
     * @param request The request containing transaction type, details, and optional policy ID.
     * @return InitiateSigningResponse containing the Turnkey Activity ID and status.
     * @throws com.maut.core.common.exception.TurnkeyOperationException if an error occurs during Turnkey operations.
     * @throws com.maut.core.common.exception.ResourceNotFoundException if the user's wallet is not found.
     * @throws com.maut.core.common.exception.InvalidRequestException if the transaction details are invalid.
     */
    InitiateSigningResponse initiateSigning(MautUser mautUser, InitiateSigningRequest request);

}
