package com.maut.core.modules.authenticator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maut.core.common.exception.AuthenticationException;
import com.maut.core.common.exception.InvalidRequestException;
import com.maut.core.common.exception.ResourceNotFoundException;
import com.maut.core.modules.authenticator.dto.CompletePasskeyRegistrationRequest;
import com.maut.core.modules.authenticator.dto.CompletePasskeyRegistrationResponse;
import com.maut.core.modules.authenticator.dto.InitiatePasskeyRegistrationResponse;
import com.maut.core.modules.authenticator.dto.ListPasskeysResponse;
import com.maut.core.modules.authenticator.dto.PasskeyListItem;
import com.maut.core.modules.authenticator.dto.VerifyPasskeyAssertionRequest;
import com.maut.core.modules.authenticator.dto.VerifyPasskeyAssertionResponse;
import com.maut.core.modules.authenticator.model.AuthenticatorType;
import com.maut.core.modules.authenticator.model.UserAuthenticator;
import com.maut.core.modules.authenticator.repository.UserAuthenticatorRepository;
import com.maut.core.modules.user.model.MautUser;
import com.maut.core.modules.wallet.model.UserWallet;
import com.maut.core.modules.wallet.repository.UserWalletRepository;
import com.maut.core.integration.turnkey.TurnkeyClient;
import com.maut.core.integration.turnkey.dto.TurnkeyInitiatePasskeyRegistrationRequest;
import com.maut.core.integration.turnkey.dto.TurnkeyInitiatePasskeyRegistrationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticatorServiceImpl implements AuthenticatorService {

    private final UserWalletRepository userWalletRepository;
    private final UserAuthenticatorRepository userAuthenticatorRepository;
    private final TurnkeyClient turnkeyClient;
    private final ObjectMapper objectMapper;

    @Override
    public InitiatePasskeyRegistrationResponse initiatePasskeyRegistration(MautUser mautUser) {
        if (mautUser == null) {
            log.error("MautUser cannot be null for initiating passkey registration.");
            throw new IllegalArgumentException("Authenticated MautUser is required for initiating passkey registration.");
        }
        log.info("Initiating passkey registration for MautUser ID: {}", mautUser.getId());

        // 1. Find the user's wallet to get the Turnkey Sub-Organization ID
        UserWallet userWallet = userWalletRepository.findByMautUser(mautUser)
            .stream().findFirst()
            .orElseThrow(() -> {
                log.warn("No UserWallet found for MautUser ID: {}. Cannot initiate passkey registration.", mautUser.getId());
                return new ResourceNotFoundException("User wallet not found, cannot initiate passkey registration.");
            });

        String turnkeySubOrganizationId = userWallet.getTurnkeySubOrganizationId();
        if (turnkeySubOrganizationId == null || turnkeySubOrganizationId.isBlank()) {
            log.error("UserWallet ID: {} for MautUser ID: {} has no Turnkey Sub-Organization ID.", userWallet.getId(), mautUser.getId());
            throw new IllegalStateException("User wallet is missing Turnkey Sub-Organization ID.");
        }

        TurnkeyInitiatePasskeyRegistrationRequest turnkeyRequest = TurnkeyInitiatePasskeyRegistrationRequest.builder()
                .mautUserId(mautUser.getId().toString())
                .turnkeySubOrganizationId(turnkeySubOrganizationId)
                .authenticatorName("New Passkey")
                .build();

        TurnkeyInitiatePasskeyRegistrationResponse turnkeyResponse = turnkeyClient.initiatePasskeyRegistration(turnkeyRequest);

        // Ensure Turnkey response is valid
        if (turnkeyResponse == null) {
            log.error("TurnkeyClient returned null response for MautUser ID: {}", mautUser.getId());
            throw new AuthenticationException("Failed to initiate passkey registration with Turnkey: No response.");
        }
        if (turnkeyResponse.getPublicKeyCredentialCreationOptions() == null || turnkeyResponse.getPublicKeyCredentialCreationOptions().isBlank()) {
            log.error("TurnkeyClient response missing public key credential creation options for MautUser ID: {}", mautUser.getId());
            throw new AuthenticationException("Failed to initiate passkey registration with Turnkey: Invalid response data.");
        }

        Map<String, Object> attestationRequestMap;
        try {
            attestationRequestMap = objectMapper.readValue(turnkeyResponse.getPublicKeyCredentialCreationOptions(), new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error parsing Turnkey public key credential creation options JSON for user ID: {}: {}", mautUser.getId(), e.getMessage());
            throw new AuthenticationException("Error processing passkey registration data from Turnkey.", e);
        }

        return InitiatePasskeyRegistrationResponse.builder()
                .turnkeyChallenge(turnkeyResponse.getChallenge())
                .turnkeyAttestationRequest(attestationRequestMap)
                .build();
    }

    @Override
    public CompletePasskeyRegistrationResponse completePasskeyRegistration(MautUser mautUser, CompletePasskeyRegistrationRequest request) {
        if (mautUser == null) {
            log.error("MautUser cannot be null for completing passkey registration.");
            throw new IllegalArgumentException("Authenticated MautUser is required for completing passkey registration.");
        }
        if (request == null || request.getTurnkeyAttestation() == null || request.getTurnkeyAttestation().isEmpty()) {
            log.error("Request or Turnkey attestation data cannot be null/empty for MautUser ID: {}", mautUser.getId());
            throw new InvalidRequestException("Turnkey attestation data is required.");
        }
        log.info("Completing passkey registration for MautUser ID: {}", mautUser.getId());

        // 1. Find the user's wallet to get the Turnkey Sub-Organization ID
        UserWallet userWallet = userWalletRepository.findByMautUser(mautUser)
            .stream().findFirst()
            .orElseThrow(() -> {
                log.warn("No UserWallet found for MautUser ID: {}. Cannot complete passkey registration.", mautUser.getId());
                return new ResourceNotFoundException("User wallet not found, cannot complete passkey registration.");
            });

        String turnkeySubOrganizationId = userWallet.getTurnkeySubOrganizationId();
        if (turnkeySubOrganizationId == null || turnkeySubOrganizationId.isBlank()) {
            log.error("UserWallet ID: {} for MautUser ID: {} has no Turnkey Sub-Organization ID.", userWallet.getId(), mautUser.getId());
            throw new IllegalStateException("User wallet is missing Turnkey Sub-Organization ID.");
        }

        // For now, using placeholder data
        log.warn("TurnkeyService not yet implemented. Simulating successful passkey registration for MautUser ID: {}", mautUser.getId());
        String placeholderTurnkeyAuthenticatorId = "turnkey_auth_id_" + java.util.UUID.randomUUID().toString();
        String placeholderExternalAuthenticatorId = "external_passkey_id_" + java.util.UUID.randomUUID().toString();

        // 2. Create and save UserAuthenticator entity
        UserAuthenticator userAuthenticator = new UserAuthenticator();
        userAuthenticator.setMautUser(mautUser);
        userAuthenticator.setAuthenticatorType(AuthenticatorType.PASSKEY);
        userAuthenticator.setTurnkeyAuthenticatorId(placeholderTurnkeyAuthenticatorId);
        userAuthenticator.setExternalAuthenticatorId(placeholderExternalAuthenticatorId); // Store the external ID (e.g., WebAuthn credential ID)
        userAuthenticator.setAuthenticatorName(request.getAuthenticatorName() != null ? request.getAuthenticatorName() : "Passkey");
        userAuthenticator.setEnabled(true);

        UserAuthenticator savedAuthenticator = userAuthenticatorRepository.save(userAuthenticator);
        log.info("New UserAuthenticator ID: {} created for MautUser ID: {}", savedAuthenticator.getId(), mautUser.getId());

        return CompletePasskeyRegistrationResponse.builder()
                .authenticatorId(savedAuthenticator.getId().toString())
                .status("SUCCESS")
                // .turnkeyAuthenticatorId(placeholderExternalAuthenticatorId) // If this is also needed in response
                .build();
    }

    @Override
    public ListPasskeysResponse listPasskeys(MautUser mautUser, int limit, int offset) {
        if (mautUser == null) {
            log.error("MautUser cannot be null for listing passkeys.");
            throw new IllegalArgumentException("Authenticated MautUser is required to list passkeys.");
        }
        if (limit <= 0) limit = 10; // Default limit
        if (offset < 0) offset = 0;   // Default offset

        log.info("Listing passkeys for MautUser ID: {}, limit: {}, offset: {}", 
            mautUser.getId(), limit, offset
        );

        // --- Placeholder for actual data fetching --- //
        // This would typically involve querying the UserAuthenticatorRepository
        // Pageable pageable = PageRequest.of(offset / limit, limit, Sort.by("createdAt").descending());
        // Page<UserAuthenticator> authenticatorPage = userAuthenticatorRepository.findByMautUser(mautUser, pageable);

        // java.util.List<PasskeyListItem> passkeyItems = authenticatorPage.getContent().stream()
        //     .map(auth -> PasskeyListItem.builder()
        //         .id(auth.getId().toString())
        //         .name(auth.getName()) // Assuming UserAuthenticator has a 'name' field
        //         .credentialId(auth.getTurnkeyAuthenticatorId()) // Or a specific credential ID field if separate
        //         .createdAt(auth.getCreatedAt())
        //         .lastUsedAt(auth.getLastUsedAt()) // Assuming UserAuthenticator has this
        //         .type(auth.getType() != null ? auth.getType().name() : "UNKNOWN")
        //         .enabled(auth.isEnabled())
        //         .build())
        //     .collect(Collectors.toList());
        // long totalPasskeys = authenticatorPage.getTotalElements();
        // --- End Placeholder --- //

        // For now, returning placeholder data
        log.warn("Database query for passkeys not yet implemented. Simulating list passkeys for MautUser ID: {}", mautUser.getId());
        java.util.List<PasskeyListItem> placeholderPasskeys = new java.util.ArrayList<>();
        java.time.Instant now = java.time.Instant.now();

        placeholderPasskeys.add(PasskeyListItem.builder()
            .id(java.util.UUID.randomUUID().toString())
            .name("MacBook Pro Touch ID")
            .credentialId("cred_id_" + java.util.UUID.randomUUID().toString().substring(0,12))
            .createdAt(now.minusSeconds(86400 * 30)) // 30 days ago
            .lastUsedAt(now.minusSeconds(3600)) // 1 hour ago
            .type("PLATFORM")
            .enabled(true)
            .build());

        if (limit > 1) {
            placeholderPasskeys.add(PasskeyListItem.builder()
                .id(java.util.UUID.randomUUID().toString())
                .name("YubiKey 5C")
                .credentialId("cred_id_" + java.util.UUID.randomUUID().toString().substring(0,12))
                .createdAt(now.minusSeconds(86400 * 10)) // 10 days ago
                .lastUsedAt(now.minusSeconds(86400 * 2)) // 2 days ago
                .type("CROSS_PLATFORM")
                .enabled(true)
                .build());
        }
        
        java.util.List<PasskeyListItem> paginatedPasskeys = placeholderPasskeys.stream()
            .skip(offset)
            .limit(limit)
            .collect(java.util.stream.Collectors.toList());

        return ListPasskeysResponse.builder()
            .passkeys(paginatedPasskeys)
            .limit(limit)
            .offset(offset)
            .totalPasskeys(placeholderPasskeys.size()) // In real scenario, this would be total count from DB query
            .build();
    }

    @Override
    public void deletePasskey(MautUser mautUser, String passkeyId) {
        if (mautUser == null) {
            log.error("MautUser cannot be null for deleting a passkey.");
            throw new IllegalArgumentException("Authenticated MautUser is required to delete a passkey.");
        }
        if (passkeyId == null || passkeyId.trim().isEmpty()) {
            log.error("Passkey ID cannot be null or empty.");
            throw new IllegalArgumentException("Passkey ID is required.");
        }

        log.info("Attempting to delete passkey with ID: {} for MautUser ID: {}", passkeyId, mautUser.getId());

        // --- Placeholder for actual data deletion --- //
        // This would typically involve:
        // 1. Finding the UserAuthenticator entity by passkeyId and mautUser.
        //    UserAuthenticator authenticator = userAuthenticatorRepository.findByIdAndMautUser(UUID.fromString(passkeyId), mautUser)
        //        .orElseThrow(() -> new com.maut.core.common.exception.ResourceNotFoundException("Passkey not found with id: " + passkeyId));
        // 2. Verifying ownership (implicitly done by querying with MautUser).
        // 3. Deleting the entity from the repository.
        //    userAuthenticatorRepository.delete(authenticator);
        // 4. Optionally, coordinating with Turnkey to de-register/delete the passkey there if necessary.
        //    log.info("Passkey {} for user {} would be deleted from Turnkey here.", passkeyId, mautUser.getId());
        // --- End Placeholder --- //

        log.warn("Passkey deletion not yet fully implemented. Simulating deletion for passkey ID: {} for MautUser ID: {}", passkeyId, mautUser.getId());
        // No actual operation performed in placeholder.
    }

    @Override
    public VerifyPasskeyAssertionResponse verifyPasskeyAssertion(MautUser mautUser, VerifyPasskeyAssertionRequest request) {
        if (request == null) {
            log.error("VerifyPasskeyAssertionRequest cannot be null.");
            throw new InvalidRequestException("Request cannot be null.");
        }
        if (request.getCredentialId() == null || request.getCredentialId().isBlank()) {
            log.error("Passkey Credential ID is required for assertion verification.");
            throw new InvalidRequestException("Passkey Credential ID is required.");
        }
        if (request.getTurnkeyAssertion() == null || request.getTurnkeyAssertion().isEmpty()) {
            log.error("Turnkey Assertion data is required for assertion verification.");
            throw new InvalidRequestException("Turnkey Assertion data is required.");
        }

        String credentialId = request.getCredentialId();
        log.info("Attempting to verify passkey assertion for credential ID: {}", credentialId);

        UserAuthenticator userAuthenticator = findAndValidateUserAuthenticator(mautUser, credentialId);

        // If mautUser was initially null, it means we identified the user via the passkey.
        // The findAndValidateUserAuthenticator method handles the case where mautUser is provided
        // and ensures the passkey belongs to that user.
        // If mautUser was null coming in, userAuthenticator.getMautUser() is the identified user.
        MautUser identifiedUser = userAuthenticator.getMautUser();
        log.info("Passkey credential ID {} is associated with MautUser ID: {}", credentialId, identifiedUser.getId());

        // --- Placeholder for Turnkey Integration --- //
        // UserWallet userWallet = userWalletRepository.findByMautUser(identifiedUser)
        //    .stream().findFirst()
        //    .orElseThrow(() -> {
        //        log.error("No UserWallet found for MautUser ID: {}. Cannot verify passkey assertion.", identifiedUser.getId());
        //        return new ResourceNotFoundException("User wallet not found, cannot verify passkey assertion.");
        //    });
        // String turnkeySubOrganizationId = userWallet.getTurnkeySubOrganizationId();
        // if (turnkeySubOrganizationId == null || turnkeySubOrganizationId.isBlank()) {
        //    log.error("UserWallet ID: {} for MautUser ID: {} has no Turnkey Sub-Organization ID.", userWallet.getId(), identifiedUser.getId());
        //    throw new IllegalStateException("User wallet is missing Turnkey Sub-Organization ID.");
        // }
        // try {
        //     log.debug("Verifying assertion with Turnkey for MautUser ID: {} and credential ID: {}", identifiedUser.getId(), credentialId);
        //     boolean turnkeyVerificationResult = turnkeyClient.verifyAssertion(turnkeySubOrganizationId, request.getTurnkeyAssertion());
        //     if (!turnkeyVerificationResult) {
        //         log.warn("Turnkey assertion verification failed for MautUser ID: {} and credential ID: {}", identifiedUser.getId(), credentialId);
        //         throw new AuthenticationException("Passkey assertion verification failed.");
        //     }
        //     log.info("Turnkey assertion verification successful for MautUser ID: {} and credential ID: {}", identifiedUser.getId(), credentialId);
        // } catch (TurnkeyOperationException e) {
        //     log.error("Turnkey operation error during assertion verification for MautUser ID: {}: {}", identifiedUser.getId(), e.getMessage(), e);
        //     throw e; // Re-throw
        // } catch (Exception e) {
        //     log.error("Unexpected error during Turnkey assertion verification for MautUser ID: {}: {}", identifiedUser.getId(), e.getMessage(), e);
        //     throw new AuthenticationException("An unexpected error occurred during passkey verification.", e);
        // }
        // --- End Placeholder --- //

        // For now, simulating successful verification
        log.warn("Turnkey assertion verification step is a placeholder. Simulating successful verification for credential ID: {}", credentialId);
        boolean verificationSuccess = true; // Placeholder

        if (verificationSuccess) {
            // Update last used timestamp
            // userAuthenticator.setLastUsedAt(Instant.now());
            // userAuthenticatorRepository.save(userAuthenticator);
            log.info("Passkey assertion verified successfully for MautUser ID: {} with authenticator ID: {}", identifiedUser.getId(), userAuthenticator.getId());
            return VerifyPasskeyAssertionResponse.builder()
                    .verified(true)
                    .authenticatorId(userAuthenticator.getId().toString()) // Return internal authenticator ID
                    .mautUserId(identifiedUser.getId().toString()) // Return identified MautUser ID
                    .message("Passkey verified successfully.")
                    .build();
        } else {
            // This path should ideally be covered by exceptions thrown from Turnkey client or validation logic
            log.warn("Passkey assertion verification failed (simulated) for MautUser ID: {} with authenticator ID: {}", identifiedUser.getId(), userAuthenticator.getId());
            return VerifyPasskeyAssertionResponse.builder()
                    .verified(false)
                    .message("Passkey verification failed.")
                    .build();
        }
    }

    /**
     * Finds a UserAuthenticator by its credential ID (external authenticator ID) and validates its status and ownership.
     * This method is intended for internal use by services like verifyPasskeyAssertion.
     *
     * @param mautUser The MautUser who is attempting to use the passkey. Can be null if the user is being identified by the passkey itself.
     * @param passkeyCredentialId The external credential ID of the passkey (e.g., from a WebAuthn assertion).
     * @return The validated UserAuthenticator if found and valid.
     * @throws AuthenticationException if the passkey is not found, not enabled, or (if mautUser is provided) does not belong to the user.
     */
    private UserAuthenticator findAndValidateUserAuthenticator(MautUser mautUser, String passkeyCredentialId) {
        if (passkeyCredentialId == null || passkeyCredentialId.isBlank()) {
            log.warn("Attempted to find authenticator with null or blank credential ID.");
            throw new AuthenticationException("Passkey credential ID cannot be blank.");
        }

        UserAuthenticator userAuthenticator = userAuthenticatorRepository.findByTurnkeyAuthenticatorId(passkeyCredentialId)
                .orElseThrow(() -> {
                    log.warn("Passkey not found for credential ID: {}", passkeyCredentialId);
                    return new AuthenticationException("Passkey not found.");
                });

        // If mautUser is provided (e.g., for 2FA or re-auth), validate ownership.
        if (mautUser != null) {
            if (userAuthenticator.getMautUser() == null) {
                 log.error("Critical: UserAuthenticator ID {} found by credential ID {} has no associated MautUser.", userAuthenticator.getId(), passkeyCredentialId);
                 throw new AuthenticationException("Passkey is not associated with any user account.");
            }
            if (!userAuthenticator.getMautUser().getId().equals(mautUser.getId())) {
                log.error("Passkey credential ID {} (UserAuthenticator ID {}) does not belong to the authenticated MautUser ID {}. It belongs to MautUser ID {}.",
                        passkeyCredentialId, userAuthenticator.getId(), mautUser.getId(), userAuthenticator.getMautUser().getId());
                throw new AuthenticationException("Passkey does not belong to the authenticated user.");
            }
        } else {
            // If mautUser is null, it means we are identifying the user by the passkey.
            // We need to ensure the found authenticator actually has an associated user.
            if (userAuthenticator.getMautUser() == null) {
                log.error("Critical: UserAuthenticator ID {} found by credential ID {} (for user identification) has no associated MautUser.", userAuthenticator.getId(), passkeyCredentialId);
                throw new AuthenticationException("Passkey is not associated with any user account, cannot identify user.");
            }
            log.info("Passkey credential ID {} successfully identified MautUser ID: {}", passkeyCredentialId, userAuthenticator.getMautUser().getId());
        }

        if (!userAuthenticator.isEnabled()) {
            log.warn("Attempt to use disabled passkey with credential ID: {} for MautUser ID: {}",
                    passkeyCredentialId, userAuthenticator.getMautUser() != null ? userAuthenticator.getMautUser().getId() : "unknown");
            throw new AuthenticationException("Passkey is disabled.");
        }

        log.debug("UserAuthenticator ID {} (Credential ID: {}) found and validated for MautUser ID: {}",
                userAuthenticator.getId(), passkeyCredentialId, userAuthenticator.getMautUser() != null ? userAuthenticator.getMautUser().getId() : "unknown (identified by passkey)");
        return userAuthenticator;
    }
}
