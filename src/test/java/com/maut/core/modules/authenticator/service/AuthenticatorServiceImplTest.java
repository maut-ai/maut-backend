package com.maut.core.modules.authenticator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maut.core.common.exception.AuthenticationException;
import com.maut.core.common.exception.InvalidRequestException;
import com.maut.core.common.exception.ResourceNotFoundException;
import com.maut.core.integration.turnkey.TurnkeyClient;
import com.maut.core.integration.turnkey.dto.TurnkeyInitiatePasskeyRegistrationRequest;
import com.maut.core.integration.turnkey.dto.TurnkeyInitiatePasskeyRegistrationResponse;
import com.maut.core.integration.turnkey.dto.TurnkeyFinalizePasskeyRegistrationRequest;
import com.maut.core.integration.turnkey.dto.TurnkeyFinalizePasskeyRegistrationResponse;
import com.maut.core.modules.authenticator.dto.InitiatePasskeyRegistrationResponse;
import com.maut.core.modules.authenticator.dto.CompletePasskeyRegistrationRequest;
import com.maut.core.modules.authenticator.dto.CompletePasskeyRegistrationResponse;
import com.maut.core.modules.authenticator.model.AuthenticatorType;
import com.maut.core.modules.authenticator.model.UserAuthenticator;
import com.maut.core.modules.authenticator.repository.UserAuthenticatorRepository;
import com.maut.core.modules.user.model.MautUser;
import com.maut.core.modules.user.repository.MautUserRepository;
import com.maut.core.modules.wallet.model.UserWallet;
import com.maut.core.modules.wallet.repository.UserWalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito; 
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*; 

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthenticatorServiceImplTest {

    @Mock
    private UserAuthenticatorRepository userAuthenticatorRepository;

    @Mock
    private MautUserRepository mautUserRepository;

    @Mock
    private UserWalletRepository userWalletRepository; 

    @Mock
    private TurnkeyClient turnkeyClient; 

    @Mock
    private ObjectMapper objectMapper; 

    @InjectMocks
    private AuthenticatorServiceImpl authenticatorService;

    private MautUser testMautUser;
    private UserWallet testUserWallet; 
    private final String expectedAuthenticatorName = "New Passkey"; 
    private final String expectedTurnkeySubOrganizationId = "test-sub-org-id"; 
    private final UUID expectedMautUserId = UUID.fromString("d3a9e3a9-1c87-435a-9bf2-03197a817d8d");
    private final UUID expectedMautUserEntityId = UUID.fromString("f0f1f2f3-f4f5-f6f7-f8f9-fafbfcfdfeff");

    @BeforeEach
    void setUp() {
        testMautUser = new MautUser();
        testMautUser.setId(expectedMautUserEntityId);
        testMautUser.setMautUserId(expectedMautUserId);
        testMautUser.setClientSystemUserId("testuser"); 

        testUserWallet = new UserWallet(); 
        testUserWallet.setId(UUID.randomUUID());
        testUserWallet.setMautUser(testMautUser);
        testUserWallet.setTurnkeySubOrganizationId(expectedTurnkeySubOrganizationId); 
    }

    // --- Test cases for initiatePasskeyRegistration --- //

    @Test
    void initiatePasskeyRegistration_successScenario_shouldReturnCorrectResponse() throws JsonProcessingException {
        // Arrange
        when(userWalletRepository.findByMautUser(testMautUser))
                .thenReturn(Collections.singletonList(testUserWallet));
        String sampleChallenge = "testChallenge";
        String samplePkOptionsJson = "{\"key\":\"value\"}";
        Map<String, Object> expectedAttestationMap = Collections.singletonMap("key", "value");

        TurnkeyInitiatePasskeyRegistrationResponse turnkeyResponse = TurnkeyInitiatePasskeyRegistrationResponse.builder()
                .challenge(sampleChallenge)
                .publicKeyCredentialCreationOptions(samplePkOptionsJson)
                .build();

        when(turnkeyClient.initiatePasskeyRegistration(any(TurnkeyInitiatePasskeyRegistrationRequest.class)))
                .thenReturn(turnkeyResponse);
        when(objectMapper.readValue(eq(samplePkOptionsJson), Mockito.<TypeReference<Map<String, Object>>>any()))
                .thenReturn(expectedAttestationMap);

        // Act
        InitiatePasskeyRegistrationResponse response = authenticatorService.initiatePasskeyRegistration(testMautUser);

        // Assert
        assertNotNull(response);
        assertEquals(sampleChallenge, response.getTurnkeyChallenge());
        assertEquals(expectedAttestationMap, response.getTurnkeyAttestationRequest());

        ArgumentCaptor<TurnkeyInitiatePasskeyRegistrationRequest> captor = ArgumentCaptor.forClass(TurnkeyInitiatePasskeyRegistrationRequest.class);
        verify(turnkeyClient).initiatePasskeyRegistration(captor.capture());
        TurnkeyInitiatePasskeyRegistrationRequest capturedRequest = captor.getValue();
        assertEquals(expectedMautUserEntityId.toString(), capturedRequest.getMautUserId());
        assertEquals(expectedTurnkeySubOrganizationId, capturedRequest.getTurnkeySubOrganizationId());
        assertEquals(expectedAuthenticatorName, capturedRequest.getAuthenticatorName());

        verify(objectMapper).readValue(eq(samplePkOptionsJson), Mockito.<TypeReference<Map<String, Object>>>any());
    }

    @Test
    void initiatePasskeyRegistration_whenTurnkeyClientReturnsNullResponse_shouldThrowAuthenticationException() {
        // Arrange
        when(userWalletRepository.findByMautUser(testMautUser))
                .thenReturn(Collections.singletonList(testUserWallet));
        when(turnkeyClient.initiatePasskeyRegistration(any(TurnkeyInitiatePasskeyRegistrationRequest.class)))
                .thenReturn(null);

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authenticatorService.initiatePasskeyRegistration(testMautUser);
        });
        assertEquals("Failed to initiate passkey registration with Turnkey: No response.", exception.getMessage());
    }

    @Test
    void initiatePasskeyRegistration_whenTurnkeyClientReturnsResponseWithNullPkOptions_shouldThrowAuthenticationException() {
        // Arrange
        when(userWalletRepository.findByMautUser(testMautUser))
                .thenReturn(Collections.singletonList(testUserWallet));
        TurnkeyInitiatePasskeyRegistrationResponse turnkeyResponse = TurnkeyInitiatePasskeyRegistrationResponse.builder()
                .challenge("testChallenge")
                .publicKeyCredentialCreationOptions(null) // Invalid part
                .build();
        when(turnkeyClient.initiatePasskeyRegistration(any(TurnkeyInitiatePasskeyRegistrationRequest.class)))
                .thenReturn(turnkeyResponse);

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authenticatorService.initiatePasskeyRegistration(testMautUser);
        });
        assertEquals("Failed to initiate passkey registration with Turnkey: Invalid response data.", exception.getMessage());
    }

    @Test
    void initiatePasskeyRegistration_whenObjectMapperThrowsJsonProcessingException_shouldThrowAuthenticationException() throws JsonProcessingException {
        // Arrange
        when(userWalletRepository.findByMautUser(testMautUser))
                .thenReturn(Collections.singletonList(testUserWallet));
        String sampleChallenge = "testChallenge";
        String samplePkOptionsJson = "{\"key\":\"value\"}";

        TurnkeyInitiatePasskeyRegistrationResponse turnkeyResponse = TurnkeyInitiatePasskeyRegistrationResponse.builder()
                .challenge(sampleChallenge)
                .publicKeyCredentialCreationOptions(samplePkOptionsJson)
                .build();

        when(turnkeyClient.initiatePasskeyRegistration(any(TurnkeyInitiatePasskeyRegistrationRequest.class)))
                .thenReturn(turnkeyResponse);
        when(objectMapper.readValue(eq(samplePkOptionsJson), Mockito.<TypeReference<Map<String, Object>>>any()))
                .thenThrow(new JsonProcessingException("Parse error") {}); 

        // Act & Assert
        Exception exception = assertThrows(AuthenticationException.class, () -> {
            authenticatorService.initiatePasskeyRegistration(testMautUser);
        });
        assertEquals("Error processing passkey registration data from Turnkey.", exception.getMessage());
    }

    // --- Test cases for completePasskeyRegistration --- //

    @Test
    void completePasskeyRegistration_successScenario_shouldReturnCorrectResponseAndSaveAuthenticator() throws JsonProcessingException {
        // Arrange
        when(userWalletRepository.findByMautUser(testMautUser))
                .thenReturn(Collections.singletonList(testUserWallet));

        String testTurnkeyChallenge = "challenge-from-initiate";
        String testClientDataJSON = "client-data-json-base64url";
        String testExternalCredentialId = "external-cred-id-raw";
        String testTurnkeyAuthenticatorId = "turnkey-auth-id-from-finalize";

        Map<String, Object> attestationMap = Map.of(
                "id", testExternalCredentialId, 
                "rawId", testExternalCredentialId, 
                "type", "public-key",
                "response", Map.of(
                        "attestationObject", "some-attestation-object-cbor",
                        "clientDataJSON", testClientDataJSON
                )
        );
        String attestationJsonString = "{\"id\":\"external-cred-id-raw\", ...}"; 

        CompletePasskeyRegistrationRequest request = CompletePasskeyRegistrationRequest.builder()
                .turnkeyAttestation(attestationMap)
                .turnkeyChallenge(testTurnkeyChallenge)
                .clientDataJSON(testClientDataJSON)
                // .authenticatorName(testAuthenticatorName) // Name is now defaulted if not provided in request
                .build();

        TurnkeyFinalizePasskeyRegistrationResponse turnkeyResponse = TurnkeyFinalizePasskeyRegistrationResponse.builder()
                .success(true)
                .turnkeyAuthenticatorId(testTurnkeyAuthenticatorId)
                .build();

        UserAuthenticator mockSavedAuthenticator = new UserAuthenticator();
        UUID mockAuthenticatorUUID = UUID.randomUUID();
        mockSavedAuthenticator.setId(mockAuthenticatorUUID); 
        mockSavedAuthenticator.setMautUser(testMautUser);
        mockSavedAuthenticator.setTurnkeyAuthenticatorId(testTurnkeyAuthenticatorId);
        mockSavedAuthenticator.setExternalAuthenticatorId(testExternalCredentialId);
        mockSavedAuthenticator.setAuthenticatorName(request.getAuthenticatorName() != null ? request.getAuthenticatorName() : "New Passkey"); // Align with service default
        mockSavedAuthenticator.setAuthenticatorType(AuthenticatorType.PASSKEY);
        mockSavedAuthenticator.setEnabled(true);

        when(objectMapper.writeValueAsString(attestationMap)).thenReturn(attestationJsonString);
        when(turnkeyClient.finalizePasskeyRegistration(any(TurnkeyFinalizePasskeyRegistrationRequest.class)))
                .thenReturn(turnkeyResponse);
        when(userAuthenticatorRepository.save(any(UserAuthenticator.class))).thenReturn(mockSavedAuthenticator);

        // Act
        CompletePasskeyRegistrationResponse serviceResponse = authenticatorService.completePasskeyRegistration(testMautUser, request);

        // Assert
        assertNotNull(serviceResponse);
        assertEquals("SUCCESS", serviceResponse.getStatus());
        assertEquals(mockAuthenticatorUUID.toString(), serviceResponse.getAuthenticatorId());
        assertEquals(testTurnkeyAuthenticatorId, serviceResponse.getTurnkeyAuthenticatorId());
        assertNull(serviceResponse.getMessage()); // Success message is typically null

        // Verify Turnkey client call
        ArgumentCaptor<TurnkeyFinalizePasskeyRegistrationRequest> turnkeyRequestCaptor = ArgumentCaptor.forClass(TurnkeyFinalizePasskeyRegistrationRequest.class);
        verify(turnkeyClient).finalizePasskeyRegistration(turnkeyRequestCaptor.capture());
        TurnkeyFinalizePasskeyRegistrationRequest capturedTurnkeyRequest = turnkeyRequestCaptor.getValue();
        assertEquals(expectedTurnkeySubOrganizationId, capturedTurnkeyRequest.getTurnkeySubOrganizationId());
        assertEquals(testTurnkeyChallenge, capturedTurnkeyRequest.getRegistrationContextId());
        assertEquals(attestationJsonString, capturedTurnkeyRequest.getAttestation());
        assertEquals(testClientDataJSON, capturedTurnkeyRequest.getClientDataJSON());

        // Verify UserAuthenticator saved
        ArgumentCaptor<UserAuthenticator> authenticatorCaptor = ArgumentCaptor.forClass(UserAuthenticator.class);
        verify(userAuthenticatorRepository).save(authenticatorCaptor.capture());
        UserAuthenticator savedAuthenticator = authenticatorCaptor.getValue();
        assertEquals(testMautUser, savedAuthenticator.getMautUser());
        assertEquals(testTurnkeyAuthenticatorId, savedAuthenticator.getTurnkeyAuthenticatorId());
        assertEquals(testExternalCredentialId, savedAuthenticator.getExternalAuthenticatorId());
        assertEquals(request.getAuthenticatorName() != null ? request.getAuthenticatorName() : "New Passkey", savedAuthenticator.getAuthenticatorName()); // Align with service default
        assertEquals(AuthenticatorType.PASSKEY, savedAuthenticator.getAuthenticatorType());
        assertTrue(savedAuthenticator.isEnabled());
    }

    @Test
    void completePasskeyRegistration_whenMautUserIsNull_shouldThrowIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authenticatorService.completePasskeyRegistration(null, CompletePasskeyRegistrationRequest.builder().build());
        });
        assertEquals("Authenticated MautUser is required for completing passkey registration.", exception.getMessage());
    }

    @Test
    void completePasskeyRegistration_whenRequestIsNull_shouldThrowInvalidRequestException() {
        // Act & Assert
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            authenticatorService.completePasskeyRegistration(testMautUser, null);
        });
        assertEquals("Turnkey attestation, challenge, and clientDataJSON are required.", exception.getMessage());
    }

    @Test
    void completePasskeyRegistration_whenTurnkeyAttestationIsNull_shouldThrowInvalidRequestException() {
        // Arrange
        CompletePasskeyRegistrationRequest request = CompletePasskeyRegistrationRequest.builder()
                .turnkeyAttestation(null)
                .turnkeyChallenge("test-challenge")
                .clientDataJSON("test-client-json")
                .build();

        // Act & Assert
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            authenticatorService.completePasskeyRegistration(testMautUser, request);
        });
        assertEquals("Turnkey attestation, challenge, and clientDataJSON are required.", exception.getMessage());
    }

    @Test
    void completePasskeyRegistration_whenTurnkeyChallengeIsNull_shouldThrowInvalidRequestException() {
        // Arrange
        CompletePasskeyRegistrationRequest request = CompletePasskeyRegistrationRequest.builder()
                .turnkeyAttestation(Map.of("id", "test-id"))
                .turnkeyChallenge(null)
                .clientDataJSON("test-client-json")
                .build();

        // Act & Assert
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            authenticatorService.completePasskeyRegistration(testMautUser, request);
        });
        assertEquals("Turnkey attestation, challenge, and clientDataJSON are required.", exception.getMessage());
    }

    @Test
    void completePasskeyRegistration_whenClientDataJSONIsBlank_shouldThrowInvalidRequestException() {
        // Arrange
        CompletePasskeyRegistrationRequest request = CompletePasskeyRegistrationRequest.builder()
                .turnkeyAttestation(Map.of("id", "test-id"))
                .turnkeyChallenge("test-challenge")
                .clientDataJSON("  ") // Blank
                .build();

        // Act & Assert
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            authenticatorService.completePasskeyRegistration(testMautUser, request);
        });
        assertEquals("Turnkey attestation, challenge, and clientDataJSON are required.", exception.getMessage());
    }

    @Test
    void completePasskeyRegistration_whenUserWalletNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        when(userWalletRepository.findByMautUser(testMautUser)).thenReturn(Collections.emptyList());
        CompletePasskeyRegistrationRequest request = CompletePasskeyRegistrationRequest.builder()
                .turnkeyAttestation(Map.of("id", "test-id"))
                .turnkeyChallenge("test-challenge")
                .clientDataJSON("test-client-json")
                .build();

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            authenticatorService.completePasskeyRegistration(testMautUser, request);
        });
        assertEquals("User wallet not found, cannot complete passkey registration.", exception.getMessage());
    }

    @Test
    void completePasskeyRegistration_whenTurnkeySubOrganizationIdIsMissing_shouldThrowIllegalStateException() {
        // Arrange
        when(userWalletRepository.findByMautUser(testMautUser))
                .thenReturn(Collections.singletonList(testUserWallet));
        testUserWallet.setTurnkeySubOrganizationId(null); // Missing ID
        CompletePasskeyRegistrationRequest request = CompletePasskeyRegistrationRequest.builder()
                .turnkeyAttestation(Map.of("id", "test-id"))
                .turnkeyChallenge("test-challenge")
                .clientDataJSON("test-client-json")
                .build();

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            authenticatorService.completePasskeyRegistration(testMautUser, request);
        });
        assertEquals("User wallet is missing Turnkey Sub-Organization ID.", exception.getMessage());
         // Reset for other tests
        testUserWallet.setTurnkeySubOrganizationId(expectedTurnkeySubOrganizationId);
    }

    @Test
    void completePasskeyRegistration_whenObjectMapperThrowsJsonProcessingException_shouldThrowAuthenticationException() throws JsonProcessingException {
        // Arrange
        when(userWalletRepository.findByMautUser(testMautUser))
                .thenReturn(Collections.singletonList(testUserWallet));
        Map<String, Object> attestationMap = Map.of("id", "test-id");
        CompletePasskeyRegistrationRequest request = CompletePasskeyRegistrationRequest.builder()
                .turnkeyAttestation(attestationMap)
                .turnkeyChallenge("test-challenge")
                .clientDataJSON("test-client-json")
                .build();
        when(objectMapper.writeValueAsString(attestationMap)).thenThrow(new JsonProcessingException("Serialization error") {});

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authenticatorService.completePasskeyRegistration(testMautUser, request);
        });
        assertEquals("Error processing attestation data.", exception.getMessage());
    }

    @Test
    void completePasskeyRegistration_whenExternalCredentialIdIsMissingInAttestation_shouldThrowInvalidRequestException() {
        // Arrange
        when(userWalletRepository.findByMautUser(testMautUser))
                .thenReturn(Collections.singletonList(testUserWallet));
        Map<String, Object> attestationMapWithoutId = Map.of("type", "public-key"); // Missing 'id' or 'rawId'
        CompletePasskeyRegistrationRequest request = CompletePasskeyRegistrationRequest.builder()
                .turnkeyAttestation(attestationMapWithoutId)
                .turnkeyChallenge("test-challenge")
                .clientDataJSON("test-client-json")
                .build();

        // Act & Assert
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            authenticatorService.completePasskeyRegistration(testMautUser, request);
        });
        assertEquals("Invalid attestation data: missing credential ID.", exception.getMessage());
    }

    @Test
    void completePasskeyRegistration_whenAttestationCredentialIdIsNotString_shouldThrowInvalidRequestException() {
        // Arrange
        when(userWalletRepository.findByMautUser(testMautUser))
                .thenReturn(Collections.singletonList(testUserWallet));
        // 'rawId' is present but is an Integer, not a String
        Map<String, Object> attestationMapWithInvalidIdType = Map.of("rawId", 12345);
        CompletePasskeyRegistrationRequest request = CompletePasskeyRegistrationRequest.builder()
                .turnkeyAttestation(attestationMapWithInvalidIdType)
                .turnkeyChallenge("test-challenge")
                .clientDataJSON("test-client-json")
                .build();

        // Act & Assert
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            authenticatorService.completePasskeyRegistration(testMautUser, request);
        });
        assertEquals("Invalid attestation data: credential ID must be a String.", exception.getMessage());
    }

    @Test
    void completePasskeyRegistration_whenTurnkeyClientThrowsException_shouldThrowAuthenticationException() throws JsonProcessingException {
        // Arrange
        when(userWalletRepository.findByMautUser(testMautUser))
                .thenReturn(Collections.singletonList(testUserWallet));
        Map<String, Object> attestationMap = Map.of("rawId", "test-raw-id");
        String attestationJsonString = "{\"rawId\":\"test-raw-id\"}";
        CompletePasskeyRegistrationRequest request = CompletePasskeyRegistrationRequest.builder()
                .turnkeyAttestation(attestationMap)
                .turnkeyChallenge("test-challenge")
                .clientDataJSON("test-client-json")
                .build();

        when(objectMapper.writeValueAsString(attestationMap)).thenReturn(attestationJsonString);
        when(turnkeyClient.finalizePasskeyRegistration(any(TurnkeyFinalizePasskeyRegistrationRequest.class)))
                .thenThrow(new RuntimeException("Turnkey communication error"));

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authenticatorService.completePasskeyRegistration(testMautUser, request);
        });
        assertEquals("Failed to communicate with Turnkey to finalize passkey registration.", exception.getMessage());
    }

    @Test
    void completePasskeyRegistration_whenTurnkeyClientReturnsNullResponse_shouldThrowAuthenticationException() throws JsonProcessingException {
        // Arrange
        when(userWalletRepository.findByMautUser(testMautUser))
                .thenReturn(Collections.singletonList(testUserWallet));
        Map<String, Object> attestationMap = Map.of("rawId", "test-raw-id");
        String attestationJsonString = "{\"rawId\":\"test-raw-id\"}";
        CompletePasskeyRegistrationRequest request = CompletePasskeyRegistrationRequest.builder()
                .turnkeyAttestation(attestationMap)
                .turnkeyChallenge("test-challenge")
                .clientDataJSON("test-client-json")
                .build();

        when(objectMapper.writeValueAsString(attestationMap)).thenReturn(attestationJsonString);
        when(turnkeyClient.finalizePasskeyRegistration(any(TurnkeyFinalizePasskeyRegistrationRequest.class)))
                .thenReturn(null);

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authenticatorService.completePasskeyRegistration(testMautUser, request);
        });
        assertEquals("Failed to complete passkey registration with Turnkey: Unknown error from Turnkey.", exception.getMessage());
    }

    @Test
    void completePasskeyRegistration_whenTurnkeyClientReturnsSuccessFalse_shouldThrowAuthenticationException() throws JsonProcessingException {
        // Arrange
        when(userWalletRepository.findByMautUser(testMautUser))
                .thenReturn(Collections.singletonList(testUserWallet));
        Map<String, Object> attestationMap = Map.of("rawId", "test-raw-id");
        String attestationJsonString = "{\"rawId\":\"test-raw-id\"}";
        CompletePasskeyRegistrationRequest request = CompletePasskeyRegistrationRequest.builder()
                .turnkeyAttestation(attestationMap)
                .turnkeyChallenge("test-challenge")
                .clientDataJSON("test-client-json")
                .build();
        TurnkeyFinalizePasskeyRegistrationResponse turnkeyResponse = TurnkeyFinalizePasskeyRegistrationResponse.builder()
                .success(false)
                .errorMessage("Invalid attestation signature")
                .build();

        when(objectMapper.writeValueAsString(attestationMap)).thenReturn(attestationJsonString);
        when(turnkeyClient.finalizePasskeyRegistration(any(TurnkeyFinalizePasskeyRegistrationRequest.class)))
                .thenReturn(turnkeyResponse);

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authenticatorService.completePasskeyRegistration(testMautUser, request);
        });
        assertEquals("Failed to complete passkey registration with Turnkey: Invalid attestation signature", exception.getMessage());
    }

    @Test
    void completePasskeyRegistration_whenTurnkeyClientReturnsResponseMissingAuthenticatorId_shouldThrowAuthenticationException() throws JsonProcessingException {
        // Arrange
        when(userWalletRepository.findByMautUser(testMautUser))
                .thenReturn(Collections.singletonList(testUserWallet));
        Map<String, Object> attestationMap = Map.of("rawId", "test-raw-id");
        String attestationJsonString = "{\"rawId\":\"test-raw-id\"}";
        CompletePasskeyRegistrationRequest request = CompletePasskeyRegistrationRequest.builder()
                .turnkeyAttestation(attestationMap)
                .turnkeyChallenge("test-challenge")
                .clientDataJSON("test-client-json")
                .build();
        TurnkeyFinalizePasskeyRegistrationResponse turnkeyResponse = TurnkeyFinalizePasskeyRegistrationResponse.builder()
                .success(true)
                .turnkeyAuthenticatorId(null) // Missing ID
                .build();

        when(objectMapper.writeValueAsString(attestationMap)).thenReturn(attestationJsonString);
        when(turnkeyClient.finalizePasskeyRegistration(any(TurnkeyFinalizePasskeyRegistrationRequest.class)))
                .thenReturn(turnkeyResponse);

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authenticatorService.completePasskeyRegistration(testMautUser, request);
        });
        assertEquals("Invalid response from Turnkey: missing authenticator ID.", exception.getMessage());
    }
}
