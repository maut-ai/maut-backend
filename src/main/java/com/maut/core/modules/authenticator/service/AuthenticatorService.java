package com.maut.core.modules.authenticator.service;

import com.maut.core.modules.authenticator.dto.CompletePasskeyRegistrationRequest;
import com.maut.core.modules.authenticator.dto.CompletePasskeyRegistrationResponse;
import com.maut.core.modules.authenticator.dto.InitiatePasskeyRegistrationResponse;
import com.maut.core.modules.authenticator.dto.ListPasskeysResponse;
import com.maut.core.modules.authenticator.dto.VerifyPasskeyAssertionRequest;
import com.maut.core.modules.authenticator.dto.VerifyPasskeyAssertionResponse;
import com.maut.core.modules.authenticator.dto.webauthn.InitiatePasskeyRegistrationServerRequestDto;
import com.maut.core.modules.authenticator.dto.webauthn.PublicKeyCredentialCreationOptionsDto;
import com.maut.core.modules.authenticator.dto.webauthn.CompletePasskeyRegistrationServerRequestDto;
import com.maut.core.modules.authenticator.dto.webauthn.PasskeyRegistrationResultDto;
import com.maut.core.modules.user.model.MautUser;
import com.maut.core.modules.user.dto.AuthenticatorDetailResponseDto;
import java.util.List;

public interface AuthenticatorService {

    /**
     * Initiates the Passkey registration process for the given MautUser.
     * This involves fetching a WebAuthn registration challenge from Turnkey.
     *
     * @param mautUser The MautUser for whom Passkey registration is being initiated. Must not be null.
     * @return InitiatePasskeyRegistrationResponse containing the Turnkey challenge and attestation request.
     * @throws com.maut.core.common.exception.TurnkeyOperationException if an error occurs during Turnkey operations.
     * @throws com.maut.core.common.exception.ResourceNotFoundException if the user's wallet (and associated Turnkey sub-org) is not found.
     */
    InitiatePasskeyRegistrationResponse initiatePasskeyRegistration(MautUser mautUser);

    /**
     * Completes the Passkey registration process for the given MautUser using the provided attestation data.
     * This involves verifying the attestation with Turnkey and creating a new UserAuthenticator record.
     *
     * @param mautUser The MautUser for whom Passkey registration is being completed. Must not be null.
     * @param request The request containing the Turnkey attestation data and optional authenticator name.
     * @return CompletePasskeyRegistrationResponse containing the new authenticator's ID and status.
     * @throws com.maut.core.common.exception.TurnkeyOperationException if an error occurs during Turnkey operations.
     * @throws com.maut.core.common.exception.ResourceNotFoundException if the user's wallet is not found.
     * @throws com.maut.core.common.exception.InvalidRequestException if the attestation data is invalid or registration fails.
     */
    CompletePasskeyRegistrationResponse completePasskeyRegistration(MautUser mautUser, CompletePasskeyRegistrationRequest request);

    /**
     * Lists passkeys registered by the authenticated user, with pagination.
     *
     * @param mautUser The MautUser for whom to list passkeys. Must not be null.
     * @param limit The maximum number of passkeys to return.
     * @param offset The number of passkeys to skip (for pagination).
     * @return ListPasskeysResponse containing the list of passkeys and pagination details.
     * @throws IllegalArgumentException if mautUser is null.
     */
    ListPasskeysResponse listPasskeys(MautUser mautUser, int limit, int offset);

    /**
     * Deletes a specific passkey for the authenticated user.
     *
     * @param mautUser The MautUser for whom to delete the passkey. Must not be null.
     * @param passkeyId The ID of the passkey to delete. Must not be null or empty.
     * @throws IllegalArgumentException if mautUser is null or passkeyId is invalid.
     * @throws com.maut.core.common.exception.ResourceNotFoundException if the passkey is not found.
     * @throws com.maut.core.common.exception.UnauthorizedOperationException if the user is not authorized to delete the passkey.
     */
    void deletePasskey(MautUser mautUser, String passkeyId);

    /**
     * Verifies a passkey assertion for the given MautUser.
     * This is typically used during a login flow to authenticate a user with a registered passkey.
     * Involves verifying the assertion data with Turnkey.
     *
     * @param mautUser The MautUser attempting to authenticate. May be null if user is not yet identified (e.g., during initial login).
     * @param request The request containing the passkey credential ID and Turnkey assertion data.
     * @return VerifyPasskeyAssertionResponse indicating if the assertion was successfully verified and the authenticator ID.
     * @throws com.maut.core.common.exception.TurnkeyOperationException if an error occurs during Turnkey operations.
     * @throws com.maut.core.common.exception.InvalidRequestException if the assertion data is invalid or verification fails.
     * @throws com.maut.core.common.exception.ResourceNotFoundException if the passkey credential is not found.
     * @throws com.maut.core.common.exception.AuthenticationException if the passkey verification fails due to mismatched user or other auth reasons.
     */
    VerifyPasskeyAssertionResponse verifyPasskeyAssertion(MautUser mautUser, VerifyPasskeyAssertionRequest request);

    // --- Vanilla WebAuthn Passkey Registration Methods (without Turnkey) ---

    /**
     * Initiates the "vanilla" WebAuthn passkey registration process for the given MautUser,
     * generating PublicKeyCredentialCreationOptions to be sent to the client.
     * This method does NOT involve Turnkey.
     *
     * @param mautUser The MautUser for whom passkey registration is being initiated. Must not be null.
     * @param requestDto Optional DTO containing client preferences like authenticator attachment. Can be null or empty.
     * @return PublicKeyCredentialCreationOptionsDto to be sent to the client to trigger navigator.credentials.create().
     * @throws com.maut.core.common.exception.InvalidRequestException if required parameters are missing or invalid.
     * @throws com.maut.core.common.exception.MautException for other underlying errors.
     */
    PublicKeyCredentialCreationOptionsDto initiateVanillaPasskeyRegistration(
            MautUser mautUser,
            InitiatePasskeyRegistrationServerRequestDto requestDto
    );

    /**
     * Completes the "vanilla" WebAuthn passkey registration process for the given MautUser
     * using the authenticator's attestation response.
     * This method verifies the attestation and, if successful, creates and stores a new WebAuthn credential.
     * This method does NOT involve Turnkey.
     *
     * @param mautUser The MautUser for whom passkey registration is being completed. Must not be null.
     * @param requestDto The DTO containing the client's attestation response (PublicKeyCredential).
     * @return PasskeyRegistrationResultDto indicating the outcome of the registration attempt.
     * @throws com.maut.core.common.exception.InvalidRequestException if the attestation data is invalid or registration fails for validation reasons.
     * @throws com.maut.core.common.exception.MautException for other underlying errors or persistence issues.
     */
    PasskeyRegistrationResultDto completeVanillaPasskeyRegistration(
            MautUser mautUser,
            CompletePasskeyRegistrationServerRequestDto requestDto
    );

    /**
     * Lists WebAuthn credentials (passkeys registered directly, not via Turnkey) for a given MautUser.
     *
     * @param mautUser The MautUser for whom to list WebAuthn credentials. Must not be null.
     * @return A list of AuthenticatorDetailResponseDto representing the WebAuthn credentials.
     */
    List<AuthenticatorDetailResponseDto> listWebauthnCredentialsForMautUser(MautUser mautUser);
}
