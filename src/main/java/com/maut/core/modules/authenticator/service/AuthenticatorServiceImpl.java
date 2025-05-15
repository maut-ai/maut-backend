package com.maut.core.modules.authenticator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maut.core.common.config.ApplicationConfig;
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
import com.maut.core.modules.authenticator.dto.webauthn.InitiatePasskeyRegistrationServerRequestDto;
import com.maut.core.modules.authenticator.dto.webauthn.PasskeyRegistrationResultDto;
import com.maut.core.modules.authenticator.dto.webauthn.PublicKeyCredentialCreationOptionsDto;
import com.maut.core.modules.authenticator.dto.webauthn.CompletePasskeyRegistrationServerRequestDto;
import com.maut.core.modules.authenticator.model.AuthenticatorType;
import com.maut.core.modules.authenticator.model.UserAuthenticator;
import com.maut.core.modules.authenticator.model.WebauthnRegistrationChallenge;
import com.maut.core.modules.authenticator.repository.UserAuthenticatorRepository;
import com.maut.core.modules.authenticator.repository.WebauthnRegistrationChallengeRepository;
import com.maut.core.modules.user.model.MautUser;
import com.maut.core.modules.user.repository.MautUserRepository;
import com.maut.core.modules.wallet.model.UserWallet;
import com.maut.core.modules.wallet.repository.UserWalletRepository;
import com.maut.core.integration.turnkey.TurnkeyClient;
import com.maut.core.integration.turnkey.dto.TurnkeyFinalizePasskeyRegistrationRequest;
import com.maut.core.integration.turnkey.dto.TurnkeyFinalizePasskeyRegistrationResponse;
import com.maut.core.integration.turnkey.dto.TurnkeyInitiatePasskeyRegistrationRequest;
import com.maut.core.integration.turnkey.dto.TurnkeyInitiatePasskeyRegistrationResponse;
import com.maut.core.integration.turnkey.dto.TurnkeyVerifyAssertionRequest;
import com.maut.core.integration.turnkey.dto.TurnkeyVerifyAssertionResponse;
import com.maut.core.integration.turnkey.exception.TurnkeyOperationException;
import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.data.*;
import com.webauthn4j.data.attestation.authenticator.AAGUID;
import com.webauthn4j.data.attestation.authenticator.AttestedCredentialData;
import com.webauthn4j.data.attestation.authenticator.AuthenticatorData;
import com.webauthn4j.data.attestation.statement.AttestationStatement;
import com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier;
import com.webauthn4j.data.client.CollectedClientData;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.server.ServerProperty;
import com.webauthn4j.util.Base64UrlUtil;

import com.webauthn4j.verifier.exception.VerificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.maut.core.modules.authenticator.model.MautUserWebauthnCredential;
import com.maut.core.modules.authenticator.repository.MautUserWebauthnCredentialRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticatorServiceImpl implements AuthenticatorService {

    private final UserAuthenticatorRepository userAuthenticatorRepository;
    private final MautUserRepository mautUserRepository;
    private final UserWalletRepository userWalletRepository;
    private final TurnkeyClient turnkeyClient;
    private final ObjectMapper objectMapper;
    private final ApplicationConfig applicationConfig;
    private final WebauthnRegistrationChallengeRepository challengeRepository;
    private final MautUserWebauthnCredentialRepository credentialRepository;
    private final WebAuthnManager webAuthnManager;

    private static final Logger logger = LoggerFactory.getLogger(AuthenticatorServiceImpl.class);

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
        // Validate new fields in the request
        if (request == null || 
            request.getTurnkeyAttestation() == null || request.getTurnkeyAttestation().isEmpty() ||
            request.getTurnkeyChallenge() == null || request.getTurnkeyChallenge().isBlank() ||
            request.getClientDataJSON() == null || request.getClientDataJSON().isBlank()) {
            log.error("Request, Turnkey attestation, challenge, or clientDataJSON cannot be null/empty for MautUser ID: {}", mautUser.getId());
            throw new InvalidRequestException("Turnkey attestation, challenge, and clientDataJSON are required.");
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

        // 2. Serialize turnkeyAttestation (PublicKeyCredential) to JSON string for Turnkey
        String attestationJson;
        String externalCredentialId; // This is the WebAuthn credential ID
        try {
            attestationJson = objectMapper.writeValueAsString(request.getTurnkeyAttestation());
            // Extract the external credential ID (WebAuthn rawId or id)
            // The PublicKeyCredential structure typically has an 'id' field (Base64URL encoded string)
            Object rawIdObject = request.getTurnkeyAttestation().get("rawId"); // Prefer rawId if available
            if (rawIdObject == null) {
                 rawIdObject = request.getTurnkeyAttestation().get("id");
            }
            if (!(rawIdObject instanceof String)) {
                log.error("Missing or invalid 'rawId' or 'id' in turnkeyAttestation for MautUser ID: {}", mautUser.getId());
                throw new InvalidRequestException("Invalid attestation data: missing credential ID.");
            }
            externalCredentialId = (String) rawIdObject;
        } catch (JsonProcessingException e) {
            log.error("Error serializing Turnkey attestation data for MautUser ID: {}: {}", mautUser.getId(), e.getMessage());
            throw new AuthenticationException("Error processing attestation data.", e);
        }

        // 3. Prepare request for Turnkey's finalizePasskeyRegistration
        TurnkeyFinalizePasskeyRegistrationRequest turnkeyRequest = TurnkeyFinalizePasskeyRegistrationRequest.builder()
                .turnkeySubOrganizationId(turnkeySubOrganizationId)
                .registrationContextId(request.getTurnkeyChallenge()) // Using challenge as context
                .attestation(attestationJson)
                .clientDataJSON(request.getClientDataJSON())
                .transports(request.getTransports())
                .build();

        // 4. Call Turnkey client
        TurnkeyFinalizePasskeyRegistrationResponse turnkeyResponse;
        try {
            turnkeyResponse = turnkeyClient.finalizePasskeyRegistration(turnkeyRequest);
        } catch (Exception e) {
            log.error("Turnkey client error during finalizePasskeyRegistration for MautUser ID: {}: {}", mautUser.getId(), e.getMessage(), e);
            throw new AuthenticationException("Failed to communicate with Turnkey to finalize passkey registration.", e);
        }

        // 5. Process Turnkey response
        if (turnkeyResponse == null || !turnkeyResponse.isSuccess()) {
            String errorMessage = (turnkeyResponse != null && turnkeyResponse.getErrorMessage() != null) ? 
                                  turnkeyResponse.getErrorMessage() : "Unknown error from Turnkey.";
            log.error("Turnkey finalizePasskeyRegistration failed for MautUser ID: {}. Reason: {}", mautUser.getId(), errorMessage);
            throw new AuthenticationException("Failed to complete passkey registration with Turnkey: " + errorMessage);
        }

        String turnkeyAuthenticatorId = turnkeyResponse.getTurnkeyAuthenticatorId();
        if (turnkeyAuthenticatorId == null || turnkeyAuthenticatorId.isBlank()) {
            log.error("Turnkey finalizePasskeyRegistration response missing turnkeyAuthenticatorId for MautUser ID: {}", mautUser.getId());
            throw new AuthenticationException("Invalid response from Turnkey: missing authenticator ID.");
        }

        // 6. Create and save UserAuthenticator entity
        UserAuthenticator userAuthenticator = new UserAuthenticator();
        userAuthenticator.setMautUser(mautUser);
        userAuthenticator.setAuthenticatorType(AuthenticatorType.PASSKEY);
        userAuthenticator.setTurnkeyAuthenticatorId(turnkeyAuthenticatorId); // From Turnkey response
        userAuthenticator.setExternalAuthenticatorId(externalCredentialId); // Extracted WebAuthn credential ID
        userAuthenticator.setAuthenticatorName(request.getAuthenticatorName() != null ? request.getAuthenticatorName() : "Passkey");
        userAuthenticator.setEnabled(true);

        UserAuthenticator savedAuthenticator = userAuthenticatorRepository.save(userAuthenticator);
        log.info("New UserAuthenticator ID: {} created for MautUser ID: {}. TurnkeyAuthenticatorID: {}", 
                 savedAuthenticator.getId(), mautUser.getId(), turnkeyAuthenticatorId);

        // 7. Return service response
        return CompletePasskeyRegistrationResponse.builder()
                .authenticatorId(savedAuthenticator.getId().toString())
                .status("SUCCESS")
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
        // Expecting turnkeyAssertion to be a map containing clientDataJSON, authenticatorData, signature, originalChallenge etc.
        if (request.getTurnkeyAssertion() == null || request.getTurnkeyAssertion().isEmpty()) {
            log.error("Turnkey Assertion data (Map) is required for assertion verification.");
            throw new InvalidRequestException("Turnkey Assertion data (Map) is required.");
        }

        String credentialId = request.getCredentialId();
        log.info("Attempting to verify passkey assertion for Maut external credential ID: {}", credentialId);

        UserAuthenticator userAuthenticator = findAndValidateUserAuthenticator(mautUser, credentialId);
        MautUser identifiedUser = userAuthenticator.getMautUser(); // User identified by the passkey

        UserWallet userWallet = userWalletRepository.findByMautUser(identifiedUser)
            .stream().findFirst()
            .orElseThrow(() -> {
                log.error("No UserWallet found for MautUser ID: {}. Cannot verify passkey assertion.", identifiedUser.getId());
                return new ResourceNotFoundException("User wallet not found, cannot verify passkey assertion.");
            });

        String turnkeySubOrganizationId = userWallet.getTurnkeySubOrganizationId();
        if (turnkeySubOrganizationId == null || turnkeySubOrganizationId.isBlank()) {
            log.error("UserWallet ID: {} for MautUser ID: {} has no Turnkey Sub-Organization ID.", userWallet.getId(), identifiedUser.getId());
            throw new IllegalStateException("User wallet is missing Turnkey Sub-Organization ID.");
        }

        Map<String, Object> assertionMap = request.getTurnkeyAssertion();
        String clientDataJSON = (String) assertionMap.get("clientDataJSON");
        String authenticatorData = (String) assertionMap.get("authenticatorData");
        String signature = (String) assertionMap.get("signature");
        String originalChallenge = (String) assertionMap.get("challenge"); // Assuming 'challenge' is the key for originalChallenge
        // The 'assertion' field for TurnkeyVerifyAssertionRequest might be the whole assertionMap as JSON
        // or a specific part of it. For now, let's assume the entire map should be sent if Turnkey expects a generic JSON object.
        // If Turnkey expects specific fields, we might need to adjust this or the TurnkeyVerifyAssertionRequest DTO.

        String assertionJsonString;
        try {
            assertionJsonString = objectMapper.writeValueAsString(assertionMap);
        } catch (JsonProcessingException e) {
            log.error("Error serializing assertion data for Turnkey request for MautUser ID {}: {}", identifiedUser.getId(), e.getMessage(), e);
            throw new AuthenticationException("Error processing assertion data.", e);
        }

        TurnkeyVerifyAssertionRequest turnkeyRequest = TurnkeyVerifyAssertionRequest.builder()
                .turnkeySubOrganizationId(turnkeySubOrganizationId)
                .passkeyCredentialId(userAuthenticator.getExternalAuthenticatorId()) // Use external ID for Turnkey
                .assertion(assertionJsonString) // Sending the whole map as JSON string
                .clientDataJSON(clientDataJSON)
                .authenticatorData(authenticatorData)
                .signature(signature)
                .originalChallenge(originalChallenge)
                .build();

        try {
            log.debug("Verifying assertion with Turnkey for MautUser ID: {} and external credential ID: {}", identifiedUser.getId(), userAuthenticator.getExternalAuthenticatorId());
            TurnkeyVerifyAssertionResponse turnkeyResponse = turnkeyClient.verifyPasskeyAssertion(turnkeyRequest);

            if (turnkeyResponse == null) {
                log.error("Turnkey assertion verification returned null response for MautUser ID: {}", identifiedUser.getId());
                throw new AuthenticationException("Passkey assertion verification failed: No response from Turnkey.");
            }

            if (!turnkeyResponse.isSuccess()) {
                log.warn("Turnkey assertion verification failed for MautUser ID: {} and external credential ID: {}. Reason: {}", identifiedUser.getId(), userAuthenticator.getExternalAuthenticatorId(), turnkeyResponse.getErrorMessage());
                throw new AuthenticationException("Passkey assertion verification failed: " + turnkeyResponse.getErrorMessage());
            }

            log.info("Turnkey assertion verification successful for MautUser ID: {} and external credential ID: {}", identifiedUser.getId(), userAuthenticator.getExternalAuthenticatorId());

            userAuthenticator.setLastUsedAt(Instant.now());
            userAuthenticatorRepository.save(userAuthenticator);
            log.info("Updated lastUsedAt for authenticator ID: {}", userAuthenticator.getId());

            return VerifyPasskeyAssertionResponse.builder()
                    .verified(true)
                    .authenticatorId(userAuthenticator.getId().toString())
                    .mautUserId(identifiedUser.getId().toString())
                    .message("Passkey verified successfully.")
                    .build();

        } catch (TurnkeyOperationException e) {
            log.error("Turnkey operation error during assertion verification for MautUser ID: {}: {}", identifiedUser.getId(), e.getMessage(), e);
            // Potentially map specific Turnkey exceptions to more user-friendly messages or specific Maut exceptions
            throw new AuthenticationException("Passkey verification failed due to a Turnkey operation error: " + e.getMessage(), e);
        } catch (Exception e) { // Catch other unexpected exceptions
            log.error("Unexpected error during Turnkey assertion verification for MautUser ID: {}: {}", identifiedUser.getId(), e.getMessage(), e);
            throw new AuthenticationException("An unexpected error occurred during passkey verification.", e);
        }
    }

    @Override
    public PublicKeyCredentialCreationOptionsDto initiateVanillaPasskeyRegistration(
            MautUser mautUser,
            InitiatePasskeyRegistrationServerRequestDto requestDto) {

        if (mautUser == null || mautUser.getId() == null) {
            log.error("MautUser and MautUser.id cannot be null for initiating passkey registration.");
            throw new InvalidRequestException("Authenticated MautUser with a valid ID is required.");
        }
        log.info("Initiating VANILLA passkey registration for MautUser ID: {}", mautUser.getId());

        ApplicationConfig.WebAuthnConfig webAuthnConfig = applicationConfig.getWebauthn();
        if (webAuthnConfig == null || webAuthnConfig.getRelyingPartyId() == null || webAuthnConfig.getRelyingPartyName() == null) {
            log.error("WebAuthn Relying Party configuration is missing or incomplete.");
            throw new RuntimeException("Server configuration error for WebAuthn.");
        }

        // 1. Relying Party (RP) Configuration
        String rpId = webAuthnConfig.getRelyingPartyId();
        String rpName = webAuthnConfig.getRelyingPartyName();
        // Origins will be checked by WebAuthn4J during completion against the actual origin of the request.

        // 2. User Configuration
        // User handle MUST be the MautUser's ID (UUID) as bytes. It should NOT be an email or username.
        // This is critical for linking the credential back to the MautUser.
        byte[] userHandle = mautUser.getId().toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        String userName = mautUser.getClientSystemUserId(); // Or a more suitable unique name within the client app
        String userDisplayName = mautUser.getClientSystemUserId(); // Or a more friendly display name

        PublicKeyCredentialUserEntity userEntity = new PublicKeyCredentialUserEntity(
                userHandle,
                userName,
                userDisplayName
        );

        // 3. Challenge
        Challenge challenge = new DefaultChallenge(); // Generates a cryptographically secure random challenge

        // 4. PublicKeyCredentialParameters (algorithms supported by RP)
        List<PublicKeyCredentialParameters> pubKeyCredParams = List.of(
                new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.ES256), // ECDSA with SHA-256
                new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.RS256)  // RSA with SHA-256
                // Potentially add more algorithms like Ed25519 (EdDSA) if supported by clients and server
        );

        // 5. Authenticator Selection Criteria (optional, based on requestDto or defaults)
        AuthenticatorSelectionCriteria authenticatorSelection = null;
        log.debug("Evaluating authenticator attachment preference. RequestDto: {}", requestDto);
        if (requestDto != null && requestDto.getAuthenticatorAttachmentPreference() != null) {
            AuthenticatorAttachment attachment = null;
            try {
                attachment = AuthenticatorAttachment.create(requestDto.getAuthenticatorAttachmentPreference());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid authenticatorAttachmentPreference '{}' provided: {}. Ignoring.", requestDto.getAuthenticatorAttachmentPreference(), e.getMessage());
            }
            if (attachment != null) {
                 authenticatorSelection = new AuthenticatorSelectionCriteria(
                    attachment,
                    ResidentKeyRequirement.PREFERRED, // residentKeyRequirement - using null for default or not specified
                    UserVerificationRequirement.PREFERRED // userVerificationRequirement - using null for default or not specified
                 );
                 // Example of more specific settings:
                 // authenticatorSelection = new AuthenticatorSelectionCriteria(
                 //    attachment,
                 //    ,
                 //    UserVerificationRequirement.PREFERRED
                 // );
            }
        }
        if (authenticatorSelection == null) { // Default if not specified or invalid
            log.info("AuthenticatorSelectionCriteria is null. Applying default: Platform authenticator, ResidentKey discouraged, UserVerification preferred.");
            authenticatorSelection = new AuthenticatorSelectionCriteria(
                AuthenticatorAttachment.PLATFORM, 
                ResidentKeyRequirement.DISCOURAGED,
                UserVerificationRequirement.PREFERRED
            );
        }

        // 6. Attestation Conveyance Preference (how much attestation information the RP wants)
        // "none" is simplest and often recommended for privacy unless specific attestation is required.
        AttestationConveyancePreference attestationPreference = AttestationConveyancePreference.NONE;

        // 7. Exclude Credentials (optional, to prevent re-registration of existing credentials)
        // List<PublicKeyCredentialDescriptor> excludeCredentialsList = getExistingCredentialsForUser(mautUser);
        // For now, we'll keep this empty. This can be an enhancement.
        List<PublicKeyCredentialDescriptor> excludeCredentialsList = List.of();

        // Build the options for the server to create PublicKeyCredentialCreationOptions
        PublicKeyCredentialCreationOptions pkcco;
        try {
            pkcco = new PublicKeyCredentialCreationOptions(
                new PublicKeyCredentialRpEntity(rpId, rpName), // Changed to RelyingPartyEntity
                userEntity,
                challenge,
                pubKeyCredParams,
                webAuthnConfig.getRegistrationTimeoutMs() != null ? webAuthnConfig.getRegistrationTimeoutMs() : 60000L, // Timeout in ms
                excludeCredentialsList,
                authenticatorSelection,
                attestationPreference,
                null // extensions can be null
            );
        } catch (Exception e) {
            log.error("Error creating PublicKeyCredentialCreationOptions with WebAuthn4J for MautUser ID {}: {}", mautUser.getId(), e.getMessage(), e);
            throw new RuntimeException("Server error while preparing passkey registration options.", e);
        }

        // 8. Store the challenge for later verification during completion
        // The challenge in pkcco is a Challenge object; we need its Base64URL string value.
        String challengeString = Base64UrlUtil.encodeToString(pkcco.getChallenge().getValue());
        OffsetDateTime expiresAt = OffsetDateTime.now().plusSeconds( (pkcco.getTimeout() != null ? pkcco.getTimeout() : 60000L) / 1000 );

        WebauthnRegistrationChallenge registrationChallenge = new WebauthnRegistrationChallenge(
                mautUser,
                challengeString,
                rpId,
                expiresAt
        );
        challengeRepository.save(registrationChallenge);
        log.info("Stored WebAuthn registration challenge ID {} for MautUser ID {}. Expires at: {}", registrationChallenge.getId(), mautUser.getId(), expiresAt);

        // 9. Map WebAuthn4J's PublicKeyCredentialCreationOptions to our DTO
        return mapWebAuthn4JOptionsToDto(pkcco);
    }

    @Override
    public PasskeyRegistrationResultDto completeVanillaPasskeyRegistration(
            MautUser mautUser,
            CompletePasskeyRegistrationServerRequestDto requestDto) {

        logger.info("Attempting to complete passkey registration for MautUser: {}", mautUser.getId());

        if (mautUser == null || mautUser.getId() == null) {
            logger.error("MautUser and MautUser.id cannot be null for completing passkey registration.");
            return PasskeyRegistrationResultDto.builder().success(false).message("Valid authenticated user is required.").build();
        }

        try {
            // 1. Decode the request data from Base64URL format
            if (requestDto == null || requestDto.getResponse() == null) {
                logger.warn("Invalid request data: response object is null for MautUser: {}", mautUser.getId());
                return PasskeyRegistrationResultDto.builder().success(false).message("Invalid request data.").build();
            }

            final byte[] clientDataJSONBytes = Base64UrlUtil.decode(requestDto.getResponse().getClientDataJSON());
            final byte[] attestationObjectBytes = Base64UrlUtil.decode(requestDto.getResponse().getAttestationObject());
            
            // 2. Parse the clientDataJSON to get the challenge
            final CollectedClientData clientData;
            try {
                // Convert byte array to string as needed for the JSON parser
                String clientDataJson = new String(clientDataJSONBytes, java.nio.charset.StandardCharsets.UTF_8);
                clientData = new ObjectConverter().getJsonConverter().readValue(clientDataJson, CollectedClientData.class);
                if (clientData == null || clientData.getChallenge() == null) {
                    logger.warn("ClientDataJSON parsing failed or challenge is missing for MautUser: {}", mautUser.getId());
                    return PasskeyRegistrationResultDto.builder().success(false).message("Invalid client data.").build();
                }
            } catch (Exception e) {
                logger.warn("Failed to parse ClientDataJSON for MautUser: {}: {}", mautUser.getId(), e.getMessage());
                return PasskeyRegistrationResultDto.builder().success(false).message("Invalid client data format.").build();
            }
            
            // Extract challenge as a Base64URL-encoded string (how it's stored in the database)
            final String challengeBase64 = new String(clientData.getChallenge().getValue(), java.nio.charset.StandardCharsets.UTF_8);
            
            // 3. Retrieve and validate the stored challenge
            final Optional<WebauthnRegistrationChallenge> challengeOpt = challengeRepository
                .findByMautUserAndChallengeAndExpiresAtAfter(mautUser, challengeBase64, OffsetDateTime.now());
                
            if (challengeOpt.isEmpty()) {
                logger.warn("Valid challenge not found for MautUser: {} with challenge: {}", mautUser.getId(), challengeBase64);
                return PasskeyRegistrationResultDto.builder().success(false).message("Challenge not found, expired, or mismatch.").build();
            }
            
            final WebauthnRegistrationChallenge storedChallenge = challengeOpt.get();

            // 4. Verify the WebAuthn registration data
            final Challenge challengeFromStorage = new DefaultChallenge(storedChallenge.getChallenge());

            final ApplicationConfig.WebAuthnConfig rpConfig = applicationConfig.getWebauthn();
            final Set<Origin> origins = rpConfig.getRelyingPartyOrigins().stream()
                                          .map(Origin::new)
                                          .collect(Collectors.toSet());
            if (origins.isEmpty()) {
                 logger.error("Relying Party Origins are not configured for RP ID: {}", rpConfig.getRelyingPartyId());
                 return PasskeyRegistrationResultDto.builder().success(false).message("Server configuration error: Relying Party Origins missing.").build();
            }
            
            final ServerProperty serverProperty = new ServerProperty(
                origins, 
                rpConfig.getRelyingPartyId(), 
                challengeFromStorage, 
                null // tokenBindingId not used
            );

            // Create proper emulation parameters (allowing self attestation and skipping attestation statement verification if needed)
            final boolean skipAttestationVerification = Boolean.TRUE.equals(rpConfig.isSkipAttestationVerification());
            final RegistrationRequest registrationRequest = new RegistrationRequest(
                attestationObjectBytes,
                clientDataJSONBytes
            );
            
            final RegistrationParameters registrationParameters = new RegistrationParameters(
                serverProperty,
                skipAttestationVerification,
                true // Support for self attestation
            );

            logger.debug("Validating WebAuthn registration request for MautUser: {} with RP ID: {}", mautUser.getId(), rpConfig.getRelyingPartyId());
            
            final RegistrationData registrationData;
            try {
                registrationData = webAuthnManager.parse(registrationRequest);
                
                // Validate the registration data
                webAuthnManager.validate(registrationData, registrationParameters);
                logger.info("WebAuthn registration validation successful for MautUser: {}", mautUser.getId());
            } catch (VerificationException e) {
                logger.warn("WebAuthn registration validation failed for MautUser: {}. Reason: {}", mautUser.getId(), e.getMessage(), e);
                return PasskeyRegistrationResultDto.builder().success(false).message("Passkey validation failed: " + e.getMessage()).build();
            }

            // 5. Extract credential data
            final AuthenticatorData authenticatorData = registrationData.getAttestationObject().getAuthenticatorData();
            final AttestedCredentialData attestedCredentialData = authenticatorData.getAttestedCredentialData(); 
            if (attestedCredentialData == null) {
                logger.error("Attested credential data is null for MautUser: {}", mautUser.getId());
                return PasskeyRegistrationResultDto.builder().success(false).message("Invalid credential data.").build();
            }

            final String credentialIdBase64 = Base64UrlUtil.encodeToString(attestedCredentialData.getCredentialId());
            
            // Check if this credential ID already exists
            if (credentialRepository.findByExternalId(credentialIdBase64).isPresent()) {
                logger.warn("Credential ID already exists: {} for MautUser: {}", credentialIdBase64, mautUser.getId());
                return PasskeyRegistrationResultDto.builder().success(false).message("This credential has already been registered.").build();
            }
            
            // 6. Get COSE public key in correct format
            final byte[] publicKeyCoseBytes;
            try {
                // Use the object converter directly to encode the public key
                publicKeyCoseBytes = new ObjectConverter().getCborConverter().writeValueAsBytes(
                    attestedCredentialData.getCOSEKey().getPublicKey()
                );
            } catch (Exception e) {
                logger.error("Failed to encode COSE public key for MautUser: {}: {}", mautUser.getId(), e.getMessage(), e);
                return PasskeyRegistrationResultDto.builder().success(false).message("Failed to process credential data.").build();
            }

            // 7. Store the new credential
            final MautUserWebauthnCredential newCredential = new MautUserWebauthnCredential();
            newCredential.setMautUser(mautUser);
            newCredential.setExternalId(credentialIdBase64); 
            newCredential.setPublicKeyCose(publicKeyCoseBytes); 
            newCredential.setSignatureCounter(authenticatorData.getSignCount());
            
            // Set attestation type if available
            final AttestationStatement attestationStatement = registrationData.getAttestationObject().getAttestationStatement();
            if (attestationStatement != null) {
                newCredential.setAttestationType(registrationData.getAttestationObject().getFormat());
            }
            
            // Set AAGUID if available
            final AAGUID aaguid = attestedCredentialData.getAaguid();
            if (aaguid != null && !aaguid.equals(AAGUID.ZERO)) {
                newCredential.setAaguid(aaguid.toString());
            }

            // Set transports if available
//            final Set<AuthenticatorTransport> transports = authenticatorData.getTransports(); // TODO
            final Set<AuthenticatorTransport> transports = null;
            if (transports != null && !transports.isEmpty()) { 
                newCredential.setTransports(transports.stream()
                    .map(AuthenticatorTransport::getValue)
                    .collect(Collectors.toList()));
            }
            
            // 8. Set friendly name (use from request if provided, otherwise generate one)
//            String friendlyName = requestDto.getFriendlyName();
            String friendlyName = "";
            if (friendlyName == null || friendlyName.isBlank()) {
                friendlyName = "Passkey-" + credentialIdBase64.substring(0, Math.min(8, credentialIdBase64.length()));
                
                // Append authenticator info if available
//                final String authenticatorAttachment = requestDto.getAuthenticatorAttachment(); // TODO
                final String authenticatorAttachment = null;
                if (authenticatorAttachment != null && !authenticatorAttachment.isBlank()) {
                    friendlyName += "-" + authenticatorAttachment;
                }
            }
            newCredential.setFriendlyName(friendlyName);
            
            // 9. Set timestamps
            final OffsetDateTime now = OffsetDateTime.now();
            newCredential.setCreatedAt(now); 
            newCredential.setLastUsedAt(now);

            // 10. Save the new credential and cleanup
            credentialRepository.save(newCredential);
            logger.info("New WebAuthn credential {} stored for MautUser: {}", credentialIdBase64, mautUser.getId());

            challengeRepository.delete(storedChallenge);
            logger.debug("Used challenge {} deleted for MautUser: {}", storedChallenge.getId(), mautUser.getId());

            // 11. Return success result
            return PasskeyRegistrationResultDto.builder()
                    .success(true)
                    .credentialId(credentialIdBase64)
                    .friendlyName(newCredential.getFriendlyName())
                    .createdAt(now.toString())
                    .message("Passkey registration successful.")
                    .build();

        } catch (Exception e) {
            logger.error("Unexpected error during passkey registration for MautUser: {}: {}", mautUser.getId(), e.getMessage(), e);
            return PasskeyRegistrationResultDto.builder().success(false).message("An unexpected error occurred: " + e.getMessage()).build();
        }
    }

    private PublicKeyCredentialCreationOptionsDto mapWebAuthn4JOptionsToDto(PublicKeyCredentialCreationOptions options) {
        PublicKeyCredentialCreationOptionsDto.RpDto rpDto = PublicKeyCredentialCreationOptionsDto.RpDto.builder()
                .id(options.getRp().getId())
                .name(options.getRp().getName())
                .build();

        PublicKeyCredentialCreationOptionsDto.UserDto userDto = PublicKeyCredentialCreationOptionsDto.UserDto.builder()
                .id(Base64UrlUtil.encodeToString(options.getUser().getId())) // User handle (MautUser.id bytes)
                .name(options.getUser().getName())
                .displayName(options.getUser().getDisplayName())
                .build();

        List<PublicKeyCredentialCreationOptionsDto.PubKeyCredParamDto> pubKeyCredParamsDto = options.getPubKeyCredParams().stream()
                .map(param -> PublicKeyCredentialCreationOptionsDto.PubKeyCredParamDto.builder()
                        .type(param.getType().getValue())
                        .alg((int) param.getAlg().getValue()) // Cast to int
                        .build())
                .collect(java.util.stream.Collectors.toList());

        PublicKeyCredentialCreationOptionsDto.AuthenticatorSelectionCriteriaDto authSelectionDto = null;
        if (options.getAuthenticatorSelection() != null) {
            AuthenticatorSelectionCriteria selection = options.getAuthenticatorSelection();
            authSelectionDto = PublicKeyCredentialCreationOptionsDto.AuthenticatorSelectionCriteriaDto.builder()
                    .authenticatorAttachment(selection.getAuthenticatorAttachment() != null ? selection.getAuthenticatorAttachment().getValue() : null)
                    .requireResidentKey(selection.isRequireResidentKey()) // Changed to isRequireResidentKey()
                    .userVerification(selection.getUserVerification() != null ? selection.getUserVerification().getValue() : null)
                    .build();
        }

        List<PublicKeyCredentialCreationOptionsDto.ExcludeCredentialDto> excludeCredentialsDto = options.getExcludeCredentials() == null ? List.of() :
                options.getExcludeCredentials().stream()
                        .map(ex -> PublicKeyCredentialCreationOptionsDto.ExcludeCredentialDto.builder()
                                .type(ex.getType().getValue())
                                .id(Base64UrlUtil.encodeToString(ex.getId()))
                                .transports(ex.getTransports() == null ? null : ex.getTransports().stream().map(AuthenticatorTransport::getValue).collect(java.util.stream.Collectors.toList()))
                                .build())
                        .collect(java.util.stream.Collectors.toList());

        return PublicKeyCredentialCreationOptionsDto.builder()
                .relyingParty(rpDto)
                .user(userDto)
                .challenge(Base64UrlUtil.encodeToString(options.getChallenge().getValue()))
                .publicKeyCredentialParameters(pubKeyCredParamsDto)
                .timeout(options.getTimeout())
                .excludeCredentials(excludeCredentialsDto.isEmpty() ? null : excludeCredentialsDto)
                .authenticatorSelection(authSelectionDto)
                .attestation(options.getAttestation() != null ? options.getAttestation().getValue() : null)
                .build();
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
            logger.warn("Attempted to find authenticator with null or blank credential ID.");
            throw new AuthenticationException("Passkey credential ID cannot be blank.");
        }

        UserAuthenticator userAuthenticator = userAuthenticatorRepository.findByTurnkeyAuthenticatorId(passkeyCredentialId)
                .orElseThrow(() -> {
                    logger.warn("Passkey not found for credential ID: {}", passkeyCredentialId);
                    return new AuthenticationException("Passkey not found.");
                });

        // If mautUser is provided (e.g., for 2FA or re-auth), validate ownership.
        if (mautUser != null) {
            if (userAuthenticator.getMautUser() == null) {
                 logger.error("Critical: UserAuthenticator ID {} found by credential ID {} has no associated MautUser.", userAuthenticator.getId(), passkeyCredentialId);
                 throw new AuthenticationException("Passkey is not associated with any user account.");
            }
            if (!userAuthenticator.getMautUser().getId().equals(mautUser.getId())) {
                logger.error("Passkey credential ID {} (UserAuthenticator ID {}) does not belong to the authenticated MautUser ID {}. It belongs to MautUser ID {}.",
                        passkeyCredentialId, userAuthenticator.getId(), mautUser.getId(), userAuthenticator.getMautUser().getId());
                throw new AuthenticationException("Passkey does not belong to the authenticated user.");
            }
        } else {
            // If mautUser is null, it means we are identifying the user by the passkey.
            // We need to ensure the found authenticator actually has an associated user.
            if (userAuthenticator.getMautUser() == null) {
                logger.error("Critical: UserAuthenticator ID {} found by credential ID {} (for user identification) has no associated MautUser.", userAuthenticator.getId(), passkeyCredentialId);
                throw new AuthenticationException("Passkey is not associated with any user account, cannot identify user.");
            }
            logger.info("Passkey credential ID {} successfully identified MautUser ID: {}", passkeyCredentialId, userAuthenticator.getMautUser().getId());
        }

        if (!userAuthenticator.isEnabled()) {
            logger.warn("Attempt to use disabled passkey with credential ID: {} for MautUser ID: {}",
                    passkeyCredentialId, userAuthenticator.getMautUser() != null ? userAuthenticator.getMautUser().getId() : "unknown");
            throw new AuthenticationException("Passkey is disabled.");
        }

        logger.debug("UserAuthenticator ID {} (Credential ID: {}) found and validated for MautUser ID: {}",
                userAuthenticator.getId(), passkeyCredentialId, userAuthenticator.getMautUser() != null ? userAuthenticator.getMautUser().getId() : "unknown (identified by passkey)");
        return userAuthenticator;
    }
}
