package com.maut.core.modules.authenticator.service;

import com.maut.core.common.exception.InvalidRequestException;
import com.maut.core.common.exception.ResourceNotFoundException;
// import com.maut.core.common.exception.TurnkeyOperationException; // For actual Turnkey calls
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
// import com.maut.core.external.turnkey.TurnkeyService; // To be created
// import com.maut.core.external.turnkey.model.WebAuthnRegistrationChallenge;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import com.maut.core.common.exception.AuthenticationException; // Assuming this exception exists or will be created

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticatorServiceImpl implements AuthenticatorService {

    private final UserWalletRepository userWalletRepository;
    private final UserAuthenticatorRepository userAuthenticatorRepository;
    // private final TurnkeyService turnkeyService; // To be uncommented

    @Override
    public InitiatePasskeyRegistrationResponse initiatePasskeyRegistration(MautUser mautUser) {
        if (mautUser == null) {
            log.error("MautUser cannot be null for initiating passkey registration.");
            throw new IllegalArgumentException("Authenticated MautUser is required for initiating passkey registration.");
        }
        log.info("Initiating passkey registration for MautUser ID: {}", mautUser.getId());

        // 1. Find the user's wallet to get the Turnkey Sub-Organization ID
        // Assuming one wallet per user for now as per current enrollment flow.
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

        // --- Placeholder for Turnkey Integration --- //
        // try {
        //     log.debug("Requesting WebAuthn registration challenge from Turnkey for sub-organization ID: {}", turnkeySubOrganizationId);
        //     WebAuthnRegistrationChallenge challenge = turnkeyService.getWebAuthnRegistrationChallenge(turnkeySubOrganizationId, mautUser.getClientSystemUserId()); // Or mautUser.getId().toString()
        //     log.info("Received WebAuthn challenge from Turnkey for MautUser ID: {}", mautUser.getId());
        //     return new InitiatePasskeyRegistrationResponse(challenge.getChallenge(), challenge.getAttestationRequest());
        // } catch (Exception e) {
        //     log.error("Error fetching WebAuthn challenge from Turnkey for MautUser ID: {}: {}", mautUser.getId(), e.getMessage(), e);
        //     throw new TurnkeyOperationException("Failed to fetch WebAuthn challenge from Turnkey: " + e.getMessage(), e);
        // }
        // --- End Placeholder --- //

        // For now, returning placeholder data
        log.warn("TurnkeyService not yet implemented. Returning placeholder challenge data for MautUser ID: {}", mautUser.getId());
        String placeholderChallenge = "placeholder_challenge_string_for_" + mautUser.getId();
        Map<String, Object> placeholderAttestationRequest = new HashMap<>();
        placeholderAttestationRequest.put("rp", Map.of("name", "Maut Demo RP", "id", "localhost"));
        placeholderAttestationRequest.put("user", Map.of("id", mautUser.getId().toString(), "name", mautUser.getClientSystemUserId(), "displayName", mautUser.getClientSystemUserId()));
        placeholderAttestationRequest.put("pubKeyCredParams", new Object[]{Map.of("type", "public-key", "alg", -7), Map.of("type", "public-key", "alg", -257)});
        placeholderAttestationRequest.put("authenticatorSelection", Map.of("authenticatorAttachment", "platform", "requireResidentKey", true, "userVerification", "required"));
        placeholderAttestationRequest.put("timeout", 360000);
        placeholderAttestationRequest.put("attestation", "direct");

        return new InitiatePasskeyRegistrationResponse(placeholderChallenge, placeholderAttestationRequest);
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

        // --- Placeholder for Turnkey Integration --- //
        // String turnkeyAuthenticatorId;
        // String externalAuthenticatorId; // e.g., aaguid or credentialId from WebAuthn
        // try {
        //     log.debug("Sending WebAuthn attestation to Turnkey for sub-organization ID: {}", turnkeySubOrganizationId);
        //     // This Turnkey service method would take subOrgId, attestation, challenge (if needed from session), userId, etc.
        //     // And return details like the new authenticator's ID in Turnkey and its external identifier.
        //     // TurnkeyCompletePasskeyRegistrationResult turnkeyResult = turnkeyService.completeWebAuthnRegistration(
        //     // turnkeySubOrganizationId, request.getTurnkeyAttestation(), mautUser.getClientSystemUserId());
        //     // turnkeyAuthenticatorId = turnkeyResult.getTurnkeyAuthenticatorId();
        //     // externalAuthenticatorId = turnkeyResult.getExternalAuthenticatorId(); // Important for identifying the passkey
        //     log.info("Passkey registration completed with Turnkey for MautUser ID: {}", mautUser.getId());
        // } catch (TurnkeyOperationException e) {
        //     log.error("Turnkey operation failed during passkey registration for MautUser ID: {}: {}", mautUser.getId(), e.getMessage(), e);
        //     throw e;
        // } catch (Exception e) {
        //     log.error("Error completing passkey registration with Turnkey for MautUser ID: {}: {}", mautUser.getId(), e.getMessage(), e);
        //     throw new TurnkeyOperationException("Failed to complete passkey registration with Turnkey: " + e.getMessage(), e);
        // }
        // --- End Placeholder --- //

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
        log.info("Attempting to verify passkey assertion.");

        if (request == null) {
            log.error("VerifyPasskeyAssertionRequest cannot be null.");
            throw new InvalidRequestException("Request cannot be null.");
        }
        if (request.getCredentialId() == null || request.getCredentialId().isBlank()) {
            log.error("Credential ID is missing in VerifyPasskeyAssertionRequest.");
            throw new InvalidRequestException("Passkey credential ID is required.");
        }
        if (request.getTurnkeyAssertion() == null || request.getTurnkeyAssertion().isEmpty()) {
            log.error("Turnkey assertion data is missing in VerifyPasskeyAssertionRequest.");
            throw new InvalidRequestException("Turnkey assertion data is required.");
        }

        String credentialId = request.getCredentialId();
        log.debug("Verifying assertion for credential ID: {}", credentialId);

        // 1. Find the UserAuthenticator by credentialId
        //    UserAuthenticator userAuthenticator = userAuthenticatorRepository.findByExternalCredentialId(credentialId) // Assuming such a method exists
        //        .orElseThrow(() -> {
        //            log.warn("No UserAuthenticator found for credential ID: {}", credentialId);
        //            return new ResourceNotFoundException("Passkey not found for the given credential ID.");
        //        });
        // For placeholder, we'll simulate finding one
        UserAuthenticator userAuthenticator = new UserAuthenticator(); // Placeholder
        userAuthenticator.setId(java.util.UUID.randomUUID());
        userAuthenticator.setExternalAuthenticatorId(credentialId);
        userAuthenticator.setEnabled(true);
        userAuthenticator.setMautUser(mautUser != null ? mautUser : new MautUser()); // Simulate association or new user identification
        if (mautUser == null) { // If mautUser was null, the passkey itself identifies the user.
            // In a real scenario, userAuthenticator.getMautUser() would be populated from the database.
            // For now, if mautUser was null, we'll assume a placeholder MautUser associated with the authenticator.
             MautUser identifiedUser = new MautUser();
             identifiedUser.setId(java.util.UUID.randomUUID());
             userAuthenticator.setMautUser(identifiedUser);
             mautUser = identifiedUser; // This is the user we've identified
             log.info("Passkey identified MautUser ID: {}", mautUser.getId());
        }

        // 2. If mautUser was provided (e.g., 2FA or re-auth), validate ownership
        // else if (mautUser != null && !userAuthenticator.getMautUser().getId().equals(mautUser.getId())) {
        //     log.error("Passkey credential ID {} does not belong to MautUser ID {}. Belongs to MautUser ID {}.",
        //             credentialId, mautUser.getId(), userAuthenticator.getMautUser().getId());
        //     throw new AuthenticationException("Passkey does not belong to the authenticated user.");
        // }

        if (!userAuthenticator.isEnabled()) {
            log.warn("Attempt to use disabled passkey with credential ID: {} for MautUser ID: {}", credentialId, userAuthenticator.getMautUser().getId());
            throw new AuthenticationException("Passkey is disabled.");
        }

        // --- Placeholder for Turnkey Integration --- //
        // try {
        //     log.debug("Verifying assertion with Turnkey for MautUser ID: {} and credential ID: {}", userAuthenticator.getMautUser().getId(), credentialId);
        //     boolean turnkeyVerificationResult = turnkeyService.verifyAssertion(mautUser.getTurnkeySubOrganizationId(), request.getTurnkeyAssertion());
        //     if (!turnkeyVerificationResult) {
        //         log.warn("Turnkey assertion verification failed for MautUser ID: {} and credential ID: {}", userAuthenticator.getMautUser().getId(), credentialId);
        //         throw new AuthenticationException("Passkey assertion verification failed.");
        //     }
        //     log.info("Turnkey assertion verification successful for MautUser ID: {} and credential ID: {}", userAuthenticator.getMautUser().getId(), credentialId);
        // } catch (TurnkeyOperationException e) {
        //     log.error("Turnkey operation error during assertion verification for MautUser ID: {}: {}", userAuthenticator.getMautUser().getId(), e.getMessage(), e);
        //     throw e; // Re-throw
        // } catch (Exception e) {
        //     log.error("Unexpected error during Turnkey assertion verification for MautUser ID: {}: {}", userAuthenticator.getMautUser().getId(), e.getMessage(), e);
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
            log.info("Passkey assertion verified successfully for MautUser ID: {} with authenticator ID: {}", userAuthenticator.getMautUser().getId(), userAuthenticator.getId());
            return VerifyPasskeyAssertionResponse.builder()
                    .verified(true)
                    .authenticatorId(userAuthenticator.getId().toString())
                    .message("Passkey verified successfully.")
                    .build();
        } else {
            // This path should ideally be covered by exceptions thrown from Turnkey client or validation logic
            log.warn("Passkey assertion verification failed (simulated) for MautUser ID: {} with authenticator ID: {}", userAuthenticator.getMautUser().getId(), userAuthenticator.getId());
            return VerifyPasskeyAssertionResponse.builder()
                    .verified(false)
                    .message("Passkey verification failed.")
                    .build();
        }
    }
}
