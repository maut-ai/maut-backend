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
import com.webauthn4j.data.attestation.authenticator.COSEKey;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.maut.core.modules.authenticator.model.MautUserWebauthnCredential;
import com.maut.core.modules.authenticator.repository.MautUserWebauthnCredentialRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.maut.core.modules.user.dto.AuthenticatorDetailResponseDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticatorServiceImpl implements AuthenticatorService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticatorServiceImpl.class);

    private final UserAuthenticatorRepository userAuthenticatorRepository;
    private final UserWalletRepository userWalletRepository;
    private final WebAuthnManager webAuthnManager;
    private final ObjectConverter objectConverter;
    private final ObjectMapper objectMapper;
    private final MautUserWebauthnCredentialRepository credentialRepository;
    private final TurnkeyClient turnkeyClient;
    private final ApplicationConfig applicationConfig;
    private final WebauthnRegistrationChallengeRepository challengeRepository;

    @Override
    public InitiatePasskeyRegistrationResponse initiatePasskeyRegistration(MautUser mautUser) {
        if (mautUser == null) {
            log.error("MautUser cannot be null for initiating passkey registration.");
            throw new IllegalArgumentException("Authenticated MautUser is required for initiating passkey registration.");
        }
        log.info("Initiating passkey registration for MautUser ID: {}", mautUser.getId());

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
        if (request == null || 
            request.getTurnkeyAttestation() == null || request.getTurnkeyAttestation().isEmpty() ||
            request.getTurnkeyChallenge() == null || request.getTurnkeyChallenge().isBlank() ||
            request.getClientDataJSON() == null || request.getClientDataJSON().isBlank()) {
            log.error("Request, Turnkey attestation, challenge, or clientDataJSON cannot be null/empty for MautUser ID: {}", mautUser.getId());
            throw new InvalidRequestException("Turnkey attestation, challenge, and clientDataJSON are required.");
        }
        log.info("Completing passkey registration for MautUser ID: {}", mautUser.getId());

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

        String attestationJson;
        String externalCredentialId;
        try {
            attestationJson = objectMapper.writeValueAsString(request.getTurnkeyAttestation());
            Object rawIdObject = request.getTurnkeyAttestation().get("rawId");
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

        TurnkeyFinalizePasskeyRegistrationRequest turnkeyRequest = TurnkeyFinalizePasskeyRegistrationRequest.builder()
                .turnkeySubOrganizationId(turnkeySubOrganizationId)
                .registrationContextId(request.getTurnkeyChallenge())
                .attestation(attestationJson)
                .clientDataJSON(request.getClientDataJSON())
                .transports(request.getTransports())
                .build();

        TurnkeyFinalizePasskeyRegistrationResponse turnkeyResponse;
        try {
            turnkeyResponse = turnkeyClient.finalizePasskeyRegistration(turnkeyRequest);
        } catch (Exception e) {
            log.error("Turnkey client error during finalizePasskeyRegistration for MautUser ID: {}: {}", mautUser.getId(), e.getMessage(), e);
            throw new AuthenticationException("Failed to communicate with Turnkey to finalize passkey registration.", e);
        }

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

        UserAuthenticator userAuthenticator = new UserAuthenticator();
        userAuthenticator.setMautUser(mautUser);
        userAuthenticator.setAuthenticatorType(AuthenticatorType.PASSKEY);
        userAuthenticator.setTurnkeyAuthenticatorId(turnkeyAuthenticatorId);
        userAuthenticator.setExternalAuthenticatorId(externalCredentialId);
        userAuthenticator.setAuthenticatorName(request.getAuthenticatorName() != null ? request.getAuthenticatorName() : "Passkey");
        userAuthenticator.setEnabled(true);

        UserAuthenticator savedAuthenticator = userAuthenticatorRepository.save(userAuthenticator);
        log.info("New UserAuthenticator ID: {} created for MautUser ID: {}. TurnkeyAuthenticatorID: {}", 
                 savedAuthenticator.getId(), mautUser.getId(), turnkeyAuthenticatorId);

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
        if (limit <= 0) limit = 10; 
        if (offset < 0) offset = 0;   

        log.info("Listing passkeys for MautUser ID: {}, limit: {}, offset: {}", 
            mautUser.getId(), limit, offset
        );

        java.util.List<PasskeyListItem> placeholderPasskeys = new java.util.ArrayList<>();
        java.time.Instant now = java.time.Instant.now();

        placeholderPasskeys.add(PasskeyListItem.builder()
            .id(java.util.UUID.randomUUID().toString())
            .name("MacBook Pro Touch ID")
            .credentialId("cred_id_" + java.util.UUID.randomUUID().toString().substring(0,12))
            .createdAt(now.minusSeconds(86400 * 30)) 
            .lastUsedAt(now.minusSeconds(3600)) 
            .type("PLATFORM")
            .enabled(true)
            .build());

        if (limit > 1) {
            placeholderPasskeys.add(PasskeyListItem.builder()
                .id(java.util.UUID.randomUUID().toString())
                .name("YubiKey 5C")
                .credentialId("cred_id_" + java.util.UUID.randomUUID().toString().substring(0,12))
                .createdAt(now.minusSeconds(86400 * 10)) 
                .lastUsedAt(now.minusSeconds(86400 * 2)) 
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
            .totalPasskeys(placeholderPasskeys.size()) 
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

        log.warn("Passkey deletion not yet fully implemented. Simulating deletion for passkey ID: {} for MautUser ID: {}", passkeyId, mautUser.getId());
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
            log.error("Turnkey Assertion data (Map) is required for assertion verification.");
            throw new InvalidRequestException("Turnkey Assertion data (Map) is required.");
        }

        String credentialId = request.getCredentialId();
        log.info("Attempting to verify passkey assertion for Maut external credential ID: {}", credentialId);

        UserAuthenticator userAuthenticator = findAndValidateUserAuthenticator(mautUser, credentialId);
        MautUser identifiedUser = userAuthenticator.getMautUser(); 

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
        String originalChallenge = (String) assertionMap.get("challenge"); 

        String assertionJsonString;
        try {
            assertionJsonString = objectMapper.writeValueAsString(assertionMap);
        } catch (JsonProcessingException e) {
            log.error("Error serializing assertion data for Turnkey request for MautUser ID {}: {}", identifiedUser.getId(), e.getMessage(), e);
            throw new AuthenticationException("Error processing assertion data.", e);
        }

        TurnkeyVerifyAssertionRequest turnkeyRequest = TurnkeyVerifyAssertionRequest.builder()
                .turnkeySubOrganizationId(turnkeySubOrganizationId)
                .passkeyCredentialId(userAuthenticator.getExternalAuthenticatorId()) 
                .assertion(assertionJsonString) 
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
            throw new AuthenticationException("Passkey verification failed due to a Turnkey operation error: " + e.getMessage(), e);
        } catch (Exception e) { 
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

        String rpId = webAuthnConfig.getRelyingPartyId();
        String rpName = webAuthnConfig.getRelyingPartyName();

        byte[] userHandle = mautUser.getId().toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        String userName = mautUser.getClientSystemUserId(); 
        String userDisplayName = mautUser.getClientSystemUserId(); 

        PublicKeyCredentialUserEntity userEntity = new PublicKeyCredentialUserEntity(
                userHandle,
                userName,
                userDisplayName
        );

        Challenge challenge = new DefaultChallenge(); 

        List<PublicKeyCredentialParameters> pubKeyCredParams = List.of(
                new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.ES256), 
                new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.RS256)  
                // Potentially add more algorithms like Ed25519 (EdDSA) if supported by clients and server
        );

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
                    ResidentKeyRequirement.PREFERRED, 
                    UserVerificationRequirement.PREFERRED 
                 );
            }
        }
        if (authenticatorSelection == null) { 
            log.info("AuthenticatorSelectionCriteria is null. Applying default: Platform authenticator, ResidentKey discouraged, UserVerification preferred.");
            authenticatorSelection = new AuthenticatorSelectionCriteria(
                AuthenticatorAttachment.PLATFORM, 
                ResidentKeyRequirement.DISCOURAGED,
                UserVerificationRequirement.PREFERRED
            );
        }

        AttestationConveyancePreference attestationPreference = AttestationConveyancePreference.NONE;

        List<PublicKeyCredentialDescriptor> excludeCredentialsList = List.of();

        PublicKeyCredentialCreationOptions pkcco;
        try {
            pkcco = new PublicKeyCredentialCreationOptions(
                new PublicKeyCredentialRpEntity(rpId, rpName), 
                userEntity,
                challenge,
                pubKeyCredParams,
                webAuthnConfig.getRegistrationTimeoutMs() != null ? webAuthnConfig.getRegistrationTimeoutMs() : 60000L, 
                excludeCredentialsList,
                authenticatorSelection,
                attestationPreference,
                null 
            );
        } catch (Exception e) {
            log.error("Error creating PublicKeyCredentialCreationOptions with WebAuthn4J for MautUser ID {}: {}", mautUser.getId(), e.getMessage(), e);
            throw new RuntimeException("Server error while preparing passkey registration options.", e);
        }

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
            final byte[] clientDataJSONBytes = Base64UrlUtil.decode(requestDto.getResponse().getClientDataJSON());
            final byte[] attestationObjectBytes = Base64UrlUtil.decode(requestDto.getResponse().getAttestationObject());
            
            final CollectedClientData clientData;
            try {
                String clientDataJson = new String(clientDataJSONBytes, java.nio.charset.StandardCharsets.UTF_8);
                clientData = this.objectConverter.getJsonConverter().readValue(clientDataJson, CollectedClientData.class);
                if (clientData == null || clientData.getChallenge() == null) {
                    logger.warn("ClientDataJSON parsing failed or challenge is missing for MautUser: {}", mautUser.getId());
                    return PasskeyRegistrationResultDto.builder().success(false).message("Invalid client data.").build();
                }
            } catch (Exception e) {
                logger.warn("Failed to parse ClientDataJSON for MautUser: {}: {}", mautUser.getId(), e.getMessage());
                return PasskeyRegistrationResultDto.builder().success(false).message("Invalid client data format.").build();
            }
            
            final String challengeBase64 = Base64UrlUtil.encodeToString(clientData.getChallenge().getValue());
            
            final Optional<WebauthnRegistrationChallenge> challengeOpt = challengeRepository
                .findByMautUserAndChallengeAndExpiresAtAfter(mautUser, challengeBase64, OffsetDateTime.now());
                
            if (challengeOpt.isEmpty()) {
                logger.warn("Valid challenge not found for MautUser: {} with challenge: {}", mautUser.getId(), challengeBase64);
                return PasskeyRegistrationResultDto.builder().success(false).message("Challenge not found, expired, or mismatch.").build();
            }
            
            final WebauthnRegistrationChallenge storedChallenge = challengeOpt.get();

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
                new DefaultChallenge(storedChallenge.getChallenge()), 
                null 
            );

            final boolean skipAttestationVerification = Boolean.TRUE.equals(rpConfig.isSkipAttestationVerification());
            final RegistrationRequest registrationRequest = new RegistrationRequest(
                attestationObjectBytes,
                clientDataJSONBytes
            );
            
            final RegistrationParameters registrationParameters = new RegistrationParameters(
                serverProperty,
                skipAttestationVerification,
                true 
            );

            logger.debug("Validating WebAuthn registration request for MautUser: {} with RP ID: {}", mautUser.getId(), rpConfig.getRelyingPartyId());
            
            final RegistrationData registrationData;
            try {
                registrationData = webAuthnManager.parse(registrationRequest);
                
                webAuthnManager.validate(registrationData, registrationParameters);
                logger.info("WebAuthn registration validation successful for MautUser: {}", mautUser.getId());
            } catch (VerificationException e) {
                logger.warn("WebAuthn registration validation failed for MautUser: {}. Reason: {}", mautUser.getId(), e.getMessage(), e);
                return PasskeyRegistrationResultDto.builder().success(false).message("Passkey validation failed: " + e.getMessage()).build();
            }

            final AuthenticatorData authenticatorData = registrationData.getAttestationObject().getAuthenticatorData();
            final AttestedCredentialData attestedCredentialData = authenticatorData.getAttestedCredentialData(); 
            if (attestedCredentialData == null) {
                logger.error("Attested credential data is null for MautUser: {}", mautUser.getId());
                return PasskeyRegistrationResultDto.builder().success(false).message("Invalid credential data.").build();
            }

            final String credentialIdBase64 = Base64UrlUtil.encodeToString(attestedCredentialData.getCredentialId());
            
            if (credentialRepository.findByExternalId(credentialIdBase64).isPresent()) {
                logger.warn("Credential ID already exists: {} for MautUser: {}", credentialIdBase64, mautUser.getId());
                return PasskeyRegistrationResultDto.builder().success(false).message("This credential has already been registered.").build();
            }
            
            COSEKey coseKey = authenticatorData.getAttestedCredentialData().getCOSEKey();
            if (coseKey == null) {
                logger.error("COSEKey is null in AttestedCredentialData for mautUserId: {}", mautUser.getId());
                throw new InvalidRequestException("COSEKey is missing from authenticator response.");
            }

            byte[] coseKeyBytes = objectConverter.getCborConverter().writeValueAsBytes(coseKey);

            String credentialIdString = Base64UrlUtil.encodeToString(authenticatorData.getAttestedCredentialData().getCredentialId());
            logger.info("Successfully processed passkey registration for mautUserId: {}. Credential ID: {}", mautUser.getId(), credentialIdString);
            
            final MautUserWebauthnCredential newCredential = new MautUserWebauthnCredential();
            newCredential.setMautUser(mautUser);
            newCredential.setExternalId(credentialIdString); 
            newCredential.setPublicKeyCose(coseKeyBytes); 
            newCredential.setSignatureCounter(authenticatorData.getSignCount());
            
            final AttestationStatement attestationStatement = registrationData.getAttestationObject().getAttestationStatement();
            if (attestationStatement != null) {
                newCredential.setAttestationType(registrationData.getAttestationObject().getFormat());
            }
            
            final AAGUID aaguid = attestedCredentialData.getAaguid();
            if (aaguid != null && !aaguid.equals(AAGUID.ZERO)) {
                newCredential.setAaguid(aaguid.toString());
            }

            String friendlyName = "Passkey-" + Instant.now().toString().substring(0, 10); 
            newCredential.setFriendlyName(friendlyName);
            
            final OffsetDateTime now = OffsetDateTime.now();
            newCredential.setCreatedAt(now); 
            newCredential.setLastUsedAt(now);

            credentialRepository.save(newCredential);
            logger.info("New WebAuthn credential {} stored for MautUser: {}", credentialIdString, mautUser.getId());

            challengeRepository.delete(storedChallenge);
            logger.debug("Used challenge {} deleted for MautUser: {}", storedChallenge.getId(), mautUser.getId());

            return PasskeyRegistrationResultDto.builder()
                    .success(true)
                    .credentialId(credentialIdString)
                    .friendlyName(newCredential.getFriendlyName())
                    .createdAt(now.toString())
                    .message("Passkey registration successful.")
                    .build();

        } catch (Exception e) {
            logger.error("Unexpected error during passkey registration for MautUser: {}: {}", mautUser.getId(), e.getMessage(), e);
            return PasskeyRegistrationResultDto.builder().success(false).message("An unexpected error occurred: " + e.getMessage()).build();
        }
    }

    @Override
    public List<AuthenticatorDetailResponseDto> listWebauthnCredentialsForMautUser(MautUser mautUser) {
        if (mautUser == null) {
            log.warn("listWebauthnCredentialsForMautUser called with null MautUser");
            return List.of(); // Or throw IllegalArgumentException based on desired contract
        }
        log.debug("Listing WebAuthn credentials for MautUser ID: {}", mautUser.getId());

        List<MautUserWebauthnCredential> credentials = credentialRepository.findAllByMautUser(mautUser);

        return credentials.stream()
                .map(cred -> new AuthenticatorDetailResponseDto(
                        cred.getId(),
                        cred.getFriendlyName(),
                        "Passkey", // Hardcoded as per request
                        cred.getCreatedAt(),
                        cred.getLastUsedAt() != null ? cred.getLastUsedAt().toInstant() : null
                ))
                .collect(Collectors.toList());
    }

    private PublicKeyCredentialCreationOptionsDto mapWebAuthn4JOptionsToDto(PublicKeyCredentialCreationOptions options) {
        PublicKeyCredentialCreationOptionsDto.RpDto rpDto = PublicKeyCredentialCreationOptionsDto.RpDto.builder()
                .id(options.getRp().getId())
                .name(options.getRp().getName())
                .build();

        PublicKeyCredentialCreationOptionsDto.UserDto userDto = PublicKeyCredentialCreationOptionsDto.UserDto.builder()
                .id(Base64UrlUtil.encodeToString(options.getUser().getId())) 
                .name(options.getUser().getName())
                .displayName(options.getUser().getDisplayName())
                .build();

        List<PublicKeyCredentialCreationOptionsDto.PubKeyCredParamDto> pubKeyCredParamsDto = options.getPubKeyCredParams().stream()
                .map(param -> PublicKeyCredentialCreationOptionsDto.PubKeyCredParamDto.builder()
                        .type(param.getType().getValue())
                        .alg((int) param.getAlg().getValue()) 
                        .build())
                .collect(java.util.stream.Collectors.toList());

        PublicKeyCredentialCreationOptionsDto.AuthenticatorSelectionCriteriaDto authSelectionDto = null;
        if (options.getAuthenticatorSelection() != null) {
            AuthenticatorSelectionCriteria selection = options.getAuthenticatorSelection();
            authSelectionDto = PublicKeyCredentialCreationOptionsDto.AuthenticatorSelectionCriteriaDto.builder()
                    .authenticatorAttachment(selection.getAuthenticatorAttachment() != null ? selection.getAuthenticatorAttachment().getValue() : null)
                    .requireResidentKey(selection.isRequireResidentKey()) 
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
