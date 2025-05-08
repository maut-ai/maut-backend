package com.maut.core.integration.turnkey;

import com.maut.core.integration.turnkey.dto.*;

/**
 * Interface for interacting with the Turnkey API.
 */
public interface TurnkeyClient {

    /**
     * Initiates the passkey registration process with Turnkey.
     *
     * @param request The request containing necessary information for initiating registration.
     * @return A response containing data needed to continue the registration on the client-side.
     */
    TurnkeyInitiatePasskeyRegistrationResponse initiatePasskeyRegistration(TurnkeyInitiatePasskeyRegistrationRequest request);

    /**
     * Finalizes the passkey registration process with Turnkey.
     *
     * @param request The request containing the client's attestation and other data.
     * @return A response indicating the outcome of the registration finalization.
     */
    TurnkeyFinalizePasskeyRegistrationResponse finalizePasskeyRegistration(TurnkeyFinalizePasskeyRegistrationRequest request);

    /**
     * Verifies a passkey assertion (login) with Turnkey.
     *
     * @param request The request containing the client's assertion data.
     * @return A response indicating the outcome of the assertion verification.
     */
    TurnkeyVerifyAssertionResponse verifyPasskeyAssertion(TurnkeyVerifyAssertionRequest request);

    // Potentially other methods like:
    // TurnkeyGetAuthenticatorResponse getAuthenticator(String turnkeyAuthenticatorId);
    // TurnkeyDeleteAuthenticatorResponse deleteAuthenticator(String turnkeyAuthenticatorId);
}
