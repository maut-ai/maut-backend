package com.maut.core.modules.authenticator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maut.core.common.exception.AuthenticationException;
import com.maut.core.integration.turnkey.TurnkeyClient;
import com.maut.core.integration.turnkey.dto.TurnkeyInitiatePasskeyRegistrationRequest;
import com.maut.core.integration.turnkey.dto.TurnkeyInitiatePasskeyRegistrationResponse;
import com.maut.core.modules.authenticator.dto.InitiatePasskeyRegistrationResponse;
import com.maut.core.modules.authenticator.dto.ListPasskeysResponse;
import com.maut.core.modules.authenticator.dto.PasskeyListItem;
import com.maut.core.modules.authenticator.dto.CompletePasskeyRegistrationRequest;
import com.maut.core.modules.authenticator.dto.CompletePasskeyRegistrationResponse;
import com.maut.core.modules.authenticator.model.AuthenticatorType;
import com.maut.core.modules.authenticator.model.UserAuthenticator;
import com.maut.core.modules.authenticator.repository.UserAuthenticatorRepository;
import com.maut.core.modules.user.model.MautUser;
import com.maut.core.modules.user.repository.MautUserRepository;
import com.maut.core.modules.wallet.model.UserWallet;
import com.maut.core.modules.wallet.repository.UserWalletRepository;
import com.maut.core.common.exception.InvalidRequestException;
import com.maut.core.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
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

    @BeforeEach
    void setUp() {
        testMautUser = new MautUser();
        testMautUser.setId(UUID.randomUUID());
        testMautUser.setMautUserId(UUID.randomUUID());
        testMautUser.setClientSystemUserId("testuser"); 

        testUserWallet = new UserWallet(); 
        testUserWallet.setId(UUID.randomUUID());
        testUserWallet.setMautUser(testMautUser);
        testUserWallet.setTurnkeySubOrganizationId(expectedTurnkeySubOrganizationId); 

        when(userWalletRepository.findByMautUser(testMautUser))
                .thenReturn(Collections.singletonList(testUserWallet));
    }

    // --- Test cases for initiatePasskeyRegistration --- //

    @Test
    void initiatePasskeyRegistration_successScenario_shouldReturnCorrectResponse() throws JsonProcessingException {
        // Arrange
        String sampleChallenge = "testChallenge";
        String samplePkOptionsJson = "{\"key\":\"value\"}";
        Map<String, Object> expectedAttestationMap = Collections.singletonMap("key", "value");

        TurnkeyInitiatePasskeyRegistrationResponse turnkeyResponse = TurnkeyInitiatePasskeyRegistrationResponse.builder()
                .challenge(sampleChallenge)
                .publicKeyCredentialCreationOptions(samplePkOptionsJson)
                .build();

        when(turnkeyClient.initiatePasskeyRegistration(any(TurnkeyInitiatePasskeyRegistrationRequest.class)))
                .thenReturn(turnkeyResponse);
        when(objectMapper.readValue(eq(samplePkOptionsJson), any(new TypeReference<Map<String, Object>>() {}.getClass())))
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
        assertEquals(testMautUser.getMautUserId().toString(), capturedRequest.getMautUserId());
        assertEquals(expectedTurnkeySubOrganizationId, capturedRequest.getTurnkeySubOrganizationId());
        assertEquals(expectedAuthenticatorName, capturedRequest.getAuthenticatorName());

        verify(objectMapper).readValue(eq(samplePkOptionsJson), any(new TypeReference<Map<String, Object>>() {}.getClass()));
    }

    @Test
    void initiatePasskeyRegistration_whenTurnkeyClientReturnsNullResponse_shouldThrowAuthenticationException() {
        // Arrange
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
        String sampleChallenge = "testChallenge";
        String samplePkOptionsJson = "{\"key\":\"value\"}";

        TurnkeyInitiatePasskeyRegistrationResponse turnkeyResponse = TurnkeyInitiatePasskeyRegistrationResponse.builder()
                .challenge(sampleChallenge)
                .publicKeyCredentialCreationOptions(samplePkOptionsJson)
                .build();

        when(turnkeyClient.initiatePasskeyRegistration(any(TurnkeyInitiatePasskeyRegistrationRequest.class)))
                .thenReturn(turnkeyResponse);
        when(objectMapper.readValue(eq(samplePkOptionsJson), any(new TypeReference<Map<String, Object>>() {}.getClass())))
                .thenThrow(new JsonProcessingException("Parse error") {}); // Simulate ObjectMapper failure

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authenticatorService.initiatePasskeyRegistration(testMautUser);
        });
        assertEquals("Error processing passkey registration data from Turnkey.", exception.getMessage());
        assertTrue(exception.getCause() instanceof JsonProcessingException);
    }

    // --- Test cases for completePasskeyRegistration --- //
    /* All completePasskeyRegistration tests collapsed */

    // --- Test cases for listPasskeys --- //
    /* All listPasskeys tests collapsed */

    // --- Test cases for deletePasskey --- //
    /* All deletePasskey tests collapsed */

    // --- Test cases for verifyPasskeyAssertion --- //
    /*
    // TODO: Add test cases for verifyPasskeyAssertion
    //  - Success scenario
    //  - MautUser not found
    //  - Authenticator not found
    //  - Turnkey client verification failure
    */

    // --- Test cases for findAndValidateUserAuthenticator --- //
    /*
    // TODO: Add test cases for findAndValidateUserAuthenticator (if to be tested directly, or through verifyPasskeyAssertion)
    //  - Authenticator found and enabled
    //  - Authenticator not found
    //  - Authenticator found but disabled
    */

}
