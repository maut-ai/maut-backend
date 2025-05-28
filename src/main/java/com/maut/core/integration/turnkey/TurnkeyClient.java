package com.maut.core.integration.turnkey;

import com.maut.core.integration.turnkey.dto.*;

/**
 * Interface for interacting with the Turnkey API.
 */
public interface TurnkeyClient {

    /**
     * Creates a new Turnkey sub-organization and simultaneously registers a user's passkey,
     * creating a private key controlled by that passkey within the new sub-organization.
     *
     * @param request The request containing sub-organization details and passkey attestation data.
     * @return A response containing details of the created sub-organization and user's key.
     */
    TurnkeyCreateSubOrganizationResponse createSubOrganizationWithPasskey(
        TurnkeyCreateSubOrganizationRequest request
    );

    /**
     * Creates a new Maut-managed private key within a specified Turnkey sub-organization.
     * This key will be controlled by the Maut backend's API key.
     *
     * @param request The request containing sub-organization ID and details for the new private key.
     * @return A response containing details of the created Maut-managed private key.
     */
    TurnkeyCreateMautManagedKeyResponse createMautManagedKey(
        TurnkeyCreateMautManagedKeyRequest request
    );

    // Potentially other methods like:
    // TurnkeyGetPolicyResponse getPolicy(String subOrganizationId, String policyId);
    // TurnkeySetPolicyResponse setPolicy(TurnkeySetPolicyRequest request);
    // TurnkeySignTransactionResponse signTransaction(TurnkeySignTransactionRequest request);


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
