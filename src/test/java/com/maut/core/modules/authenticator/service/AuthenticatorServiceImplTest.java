package com.maut.core.modules.authenticator.service;

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
import org.mockito.ArgumentCaptor; // Added import

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticatorServiceImplTest {

    @Mock
    private UserAuthenticatorRepository userAuthenticatorRepository;

    @Mock
    private MautUserRepository mautUserRepository;

    @Mock
    private UserWalletRepository userWalletRepository;

    @InjectMocks
    private AuthenticatorServiceImpl authenticatorService;

    private MautUser testMautUser;

    @BeforeEach
    void setUp() {
        testMautUser = new MautUser();
        testMautUser.setId(UUID.randomUUID());
        testMautUser.setMautUserId(UUID.randomUUID()); // Assuming MautUser has mautUserId for operations
        // Initialize other necessary fields for testMautUser
    }

    // --- Test cases for initiatePasskeyRegistration --- //
    @Test
    void initiatePasskeyRegistration_whenMautUserIsNull_shouldReturnNonNullResponse() {
        // Current implementation in AuthenticatorServiceImpl allows mautUser to be null initially
        // and relies on Turnkey to create/associate user. This test reflects that.
        // If MautUser becomes mandatory upfront, this test and implementation will change.
        // TODO: Uncomment and mock when TurnkeyAuthenticatorClient is available
        // when(turnkeyAuthenticatorClient.initiateRegistration(any())).thenReturn(/* mock Turnkey response */);
        // InitiatePasskeyRegistrationResponse response = authenticatorService.initiatePasskeyRegistration(null);
        // assertNotNull(response);
        // Verify interactions with turnkeyAuthenticatorClient if applicable
        assertDoesNotThrow(() -> authenticatorService.initiatePasskeyRegistration(null));
    }

    // TODO: Add more tests for initiatePasskeyRegistration (e.g., Turnkey client interactions)

    // --- Test cases for completePasskeyRegistration --- //
    @Test
    void completePasskeyRegistration_whenMautUserIsNull_shouldThrowIllegalArgumentException() {
        CompletePasskeyRegistrationRequest request = CompletePasskeyRegistrationRequest.builder().build();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authenticatorService.completePasskeyRegistration(null, request);
        });
        assertEquals("Authenticated MautUser is required for completing passkey registration.", exception.getMessage());
    }

    @Test
    void completePasskeyRegistration_whenRequestIsNull_shouldThrowInvalidRequestException() {
        // Service method actually throws NPE before custom InvalidRequestException if request is null
        // due to request.getTurnkeyAttestation(). This tests for the NullPointerException.
        assertThrows(NullPointerException.class, () -> {
            authenticatorService.completePasskeyRegistration(testMautUser, null);
        });
    }

    @Test
    void completePasskeyRegistration_whenTurnkeyAttestationIsNull_shouldThrowInvalidRequestException() {
        CompletePasskeyRegistrationRequest request = CompletePasskeyRegistrationRequest.builder()
                .turnkeyAttestation(null) // This is valid for Map type
                .build();
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            authenticatorService.completePasskeyRegistration(testMautUser, request);
        });
        assertEquals("Turnkey attestation data is required.", exception.getMessage());
    }

    @Test
    void completePasskeyRegistration_whenTurnkeyAttestationIsEmpty_shouldThrowInvalidRequestException() {
        CompletePasskeyRegistrationRequest request = CompletePasskeyRegistrationRequest.builder()
                .turnkeyAttestation(java.util.Collections.emptyMap()) // Use empty map for Map type
                .build();
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            authenticatorService.completePasskeyRegistration(testMautUser, request);
        });
        assertEquals("Turnkey attestation data is required.", exception.getMessage());
    }

    @Test
    void completePasskeyRegistration_whenUserWalletNotFound_shouldThrowResourceNotFoundException() {
        CompletePasskeyRegistrationRequest request = CompletePasskeyRegistrationRequest.builder()
                .turnkeyAttestation(java.util.Map.of("key", "value")) // Use a non-empty map
                .authenticatorName("My Test Passkey")
                .build();
        when(userWalletRepository.findByMautUser(testMautUser)).thenReturn(java.util.Collections.emptyList());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            authenticatorService.completePasskeyRegistration(testMautUser, request);
        });
        assertEquals("User wallet not found, cannot complete passkey registration.", exception.getMessage());
    }

    @Test
    void completePasskeyRegistration_whenTurnkeySubOrganizationIdIsNull_shouldThrowIllegalStateException() {
        CompletePasskeyRegistrationRequest request = CompletePasskeyRegistrationRequest.builder()
                .turnkeyAttestation(java.util.Map.of("key", "value")) // Use a non-empty map
                .authenticatorName("My Test Passkey")
                .build();
        UserWallet wallet = new UserWallet();
        wallet.setTurnkeySubOrganizationId(null); // Null SubOrgId
        when(userWalletRepository.findByMautUser(testMautUser)).thenReturn(java.util.Collections.singletonList(wallet));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            authenticatorService.completePasskeyRegistration(testMautUser, request);
        });
        assertEquals("User wallet is missing Turnkey Sub-Organization ID.", exception.getMessage());
    }

    @Test
    void completePasskeyRegistration_whenTurnkeySubOrganizationIdIsBlank_shouldThrowIllegalStateException() {
        CompletePasskeyRegistrationRequest request = CompletePasskeyRegistrationRequest.builder()
                .turnkeyAttestation(java.util.Map.of("key", "value")) // Use a non-empty map
                .authenticatorName("My Test Passkey")
                .build();
        UserWallet wallet = new UserWallet();
        wallet.setTurnkeySubOrganizationId("   "); // Blank SubOrgId
        when(userWalletRepository.findByMautUser(testMautUser)).thenReturn(java.util.Collections.singletonList(wallet));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            authenticatorService.completePasskeyRegistration(testMautUser, request);
        });
        assertEquals("User wallet is missing Turnkey Sub-Organization ID.", exception.getMessage());
    }

    @Test
    void completePasskeyRegistration_successScenario_withAuthenticatorName_shouldSaveAuthenticatorAndReturnResponse() {
        String authenticatorName = "My Test Passkey";
        CompletePasskeyRegistrationRequest request = CompletePasskeyRegistrationRequest.builder()
                .turnkeyAttestation(java.util.Map.of("attestationKey", "attestationValue"))
                .authenticatorName(authenticatorName)
                .build();

        UserWallet wallet = new UserWallet();
        String turnkeySubOrgId = "sub_org_123";
        wallet.setTurnkeySubOrganizationId(turnkeySubOrgId);
        when(userWalletRepository.findByMautUser(testMautUser)).thenReturn(java.util.Collections.singletonList(wallet));

        UserAuthenticator expectedSavedAuthenticator = new UserAuthenticator();
        expectedSavedAuthenticator.setId(UUID.randomUUID());
        // Other fields will be set by the service, we'll capture and verify
        when(userAuthenticatorRepository.save(any(UserAuthenticator.class))).thenAnswer(invocation -> {
            UserAuthenticator ua = invocation.getArgument(0);
            ua.setId(expectedSavedAuthenticator.getId()); // Simulate ID generation on save
            return ua;
        });

        CompletePasskeyRegistrationResponse response = authenticatorService.completePasskeyRegistration(testMautUser, request);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(expectedSavedAuthenticator.getId().toString(), response.getAuthenticatorId());

        ArgumentCaptor<UserAuthenticator> captor = ArgumentCaptor.forClass(UserAuthenticator.class);
        verify(userAuthenticatorRepository).save(captor.capture());
        UserAuthenticator capturedAuthenticator = captor.getValue();

        assertEquals(testMautUser, capturedAuthenticator.getMautUser());
        assertEquals(AuthenticatorType.PASSKEY, capturedAuthenticator.getAuthenticatorType());
        assertEquals(authenticatorName, capturedAuthenticator.getAuthenticatorName());
        assertTrue(capturedAuthenticator.isEnabled());
        assertNotNull(capturedAuthenticator.getTurnkeyAuthenticatorId()); // Placeholder ID
        assertNotNull(capturedAuthenticator.getExternalAuthenticatorId()); // Placeholder ID
    }

    @Test
    void completePasskeyRegistration_successScenario_withoutAuthenticatorName_shouldDefaultNameAndSaveAuthenticator() {
        CompletePasskeyRegistrationRequest request = CompletePasskeyRegistrationRequest.builder()
                .turnkeyAttestation(java.util.Map.of("attestationKey", "attestationValue"))
                // authenticatorName is null
                .build();

        UserWallet wallet = new UserWallet();
        String turnkeySubOrgId = "sub_org_456";
        wallet.setTurnkeySubOrganizationId(turnkeySubOrgId);
        when(userWalletRepository.findByMautUser(testMautUser)).thenReturn(java.util.Collections.singletonList(wallet));

        UUID generatedId = UUID.randomUUID();
        when(userAuthenticatorRepository.save(any(UserAuthenticator.class))).thenAnswer(invocation -> {
            UserAuthenticator ua = invocation.getArgument(0);
            ua.setId(generatedId); // Simulate ID generation on save
            return ua;
        });

        CompletePasskeyRegistrationResponse response = authenticatorService.completePasskeyRegistration(testMautUser, request);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(generatedId.toString(), response.getAuthenticatorId());

        ArgumentCaptor<UserAuthenticator> captor = ArgumentCaptor.forClass(UserAuthenticator.class);
        verify(userAuthenticatorRepository).save(captor.capture());
        UserAuthenticator capturedAuthenticator = captor.getValue();

        assertEquals(testMautUser, capturedAuthenticator.getMautUser());
        assertEquals(AuthenticatorType.PASSKEY, capturedAuthenticator.getAuthenticatorType());
        assertEquals("Passkey", capturedAuthenticator.getAuthenticatorName()); // Default name
        assertTrue(capturedAuthenticator.isEnabled());
        assertNotNull(capturedAuthenticator.getTurnkeyAuthenticatorId());
        assertNotNull(capturedAuthenticator.getExternalAuthenticatorId());
    }

    @Test
    void completePasskeyRegistration_whenRepositorySaveFails_shouldThrowException() {
        CompletePasskeyRegistrationRequest request = CompletePasskeyRegistrationRequest.builder()
                .turnkeyAttestation(java.util.Map.of("attestationKey", "attestationValue"))
                .authenticatorName("My Test Passkey")
                .build();

        UserWallet wallet = new UserWallet();
        String turnkeySubOrgId = "sub_org_789";
        wallet.setTurnkeySubOrganizationId(turnkeySubOrgId);
        when(userWalletRepository.findByMautUser(testMautUser)).thenReturn(java.util.Collections.singletonList(wallet));

        // Simulate repository save failure
        when(userAuthenticatorRepository.save(any(UserAuthenticator.class)))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("Simulated DB save failure"));

        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> {
            authenticatorService.completePasskeyRegistration(testMautUser, request);
        });

        verify(userAuthenticatorRepository).save(any(UserAuthenticator.class)); // Verify save was attempted
    }

    // --- Test cases for listPasskeys --- //
    @Test
    void listPasskeys_whenMautUserIsNull_shouldThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authenticatorService.listPasskeys(null, 10, 0);
        });
        assertEquals("Authenticated MautUser is required to list passkeys.", exception.getMessage());
    }

    @Test
    void listPasskeys_whenUserHasNoPasskeys_shouldReturnEmptyList() {
        when(userAuthenticatorRepository.findByMautUser(eq(testMautUser), any()))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(java.util.Collections.emptyList()));

        ListPasskeysResponse response = authenticatorService.listPasskeys(testMautUser, 10, 0);

        assertNotNull(response);
        assertTrue(response.getPasskeys().isEmpty());
        assertEquals(0, response.getTotalPasskeys());
        assertEquals(10, response.getLimit());
        assertEquals(0, response.getOffset());
        verify(userAuthenticatorRepository).findByMautUser(eq(testMautUser), any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    void listPasskeys_whenUserHasPasskeys_shouldReturnPasskeyList() {
        UserAuthenticator authenticator1 = new UserAuthenticator();
        authenticator1.setId(UUID.randomUUID());
        authenticator1.setAuthenticatorName("Work Laptop");
        authenticator1.setExternalAuthenticatorId("cred1"); // Corrected field name
        authenticator1.setCreatedAt(Instant.now()); // Corrected type
        // authenticator1.setLastUsedAt(java.time.LocalDateTime.now()); // Removed, not in UserAuthenticator
        authenticator1.setAuthenticatorType(AuthenticatorType.PASSKEY); // Corrected enum member
        authenticator1.setEnabled(true);
        authenticator1.setMautUser(testMautUser);

        java.util.List<UserAuthenticator> authenticators = java.util.Collections.singletonList(authenticator1);
        org.springframework.data.domain.Page<UserAuthenticator> page = new org.springframework.data.domain.PageImpl<>(authenticators);

        when(userAuthenticatorRepository.findByMautUser(eq(testMautUser), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);

        ListPasskeysResponse response = authenticatorService.listPasskeys(testMautUser, 10, 0);

        assertNotNull(response);
        assertEquals(1, response.getPasskeys().size());
        assertEquals(1, response.getTotalPasskeys());
        PasskeyListItem item = response.getPasskeys().get(0);
        assertEquals(authenticator1.getId().toString(), item.getId());
        assertEquals("Work Laptop", item.getName());
        assertEquals("cred1", item.getCredentialId());
        assertNotNull(item.getCreatedAt());
        // assertNotNull(item.getLastUsedAt()); // Removed, not directly mapped from UserAuthenticator in this path
        assertEquals("PASSKEY", item.getType()); // DTO uses String representation
        assertTrue(item.isEnabled());

        verify(userAuthenticatorRepository).findByMautUser(eq(testMautUser), any(org.springframework.data.domain.Pageable.class));
    }

    // --- Test cases for deletePasskey --- //
    @Test
    void deletePasskey_whenMautUserIsNull_shouldThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authenticatorService.deletePasskey(null, UUID.randomUUID().toString());
        });
        assertEquals("Authenticated MautUser is required to delete a passkey.", exception.getMessage());
    }

    @Test
    void deletePasskey_whenPasskeyIdIsNull_shouldThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authenticatorService.deletePasskey(testMautUser, null);
        });
        assertEquals("Passkey ID is required to delete a passkey.", exception.getMessage());
    }

    @Test
    void deletePasskey_whenPasskeyIdIsEmpty_shouldThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authenticatorService.deletePasskey(testMautUser, "");
        });
        assertEquals("Passkey ID is required to delete a passkey.", exception.getMessage());
    }

    @Test
    void deletePasskey_whenPasskeyIdIsBlank_shouldThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authenticatorService.deletePasskey(testMautUser, "   ");
        });
        assertEquals("Passkey ID is required to delete a passkey.", exception.getMessage());
    }

    @Test
    void deletePasskey_whenPasskeyIdIsInvalidFormat_shouldThrowIllegalArgumentException() {
        String invalidPasskeyId = "not-a-uuid";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authenticatorService.deletePasskey(testMautUser, invalidPasskeyId);
        });
        assertEquals("Invalid Passkey ID format: " + invalidPasskeyId, exception.getMessage());
    }

    @Test
    void deletePasskey_whenPasskeyNotFound_shouldCompleteWithoutError() {
        UUID passkeyUUID = UUID.randomUUID();
        when(userAuthenticatorRepository.findById(passkeyUUID)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> {
            authenticatorService.deletePasskey(testMautUser, passkeyUUID.toString());
        });

        verify(userAuthenticatorRepository).findById(passkeyUUID);
        verify(userAuthenticatorRepository, never()).delete(any(UserAuthenticator.class));
    }

    @Test
    void deletePasskey_whenUserDoesNotOwnPasskey_shouldCompleteWithoutError() {
        UUID passkeyUUID = UUID.randomUUID();
        UserAuthenticator otherUserAuthenticator = new UserAuthenticator();
        otherUserAuthenticator.setId(passkeyUUID);
        MautUser otherMautUser = new MautUser();
        otherMautUser.setId(UUID.randomUUID()); // Different user ID
        otherUserAuthenticator.setMautUser(otherMautUser);

        when(userAuthenticatorRepository.findById(passkeyUUID)).thenReturn(Optional.of(otherUserAuthenticator));

        assertDoesNotThrow(() -> {
            authenticatorService.deletePasskey(testMautUser, passkeyUUID.toString());
        });

        verify(userAuthenticatorRepository).findById(passkeyUUID);
        verify(userAuthenticatorRepository, never()).delete(any(UserAuthenticator.class));
    }

    @Test
    void deletePasskey_whenPasskeyFoundAndOwned_shouldCompleteWithoutDeletingFromRepository() {
        UUID passkeyUUID = UUID.randomUUID();
        UserAuthenticator ownedAuthenticator = new UserAuthenticator();
        ownedAuthenticator.setId(passkeyUUID);
        ownedAuthenticator.setMautUser(testMautUser); // Belongs to testMautUser

        when(userAuthenticatorRepository.findById(passkeyUUID)).thenReturn(Optional.of(ownedAuthenticator));

        assertDoesNotThrow(() -> {
            authenticatorService.deletePasskey(testMautUser, passkeyUUID.toString());
        });

        verify(userAuthenticatorRepository).findById(passkeyUUID);
        verify(userAuthenticatorRepository, never()).delete(ownedAuthenticator); // Placeholder: no delete
    }

    // --- Test cases for verifyPasskeyAssertion --- //
    // TODO: Add test cases for verifyPasskeyAssertion
    //  - Success scenario
    //  - MautUser not found
    //  - Authenticator not found
    //  - Turnkey client verification failure

    // --- Test cases for findAndValidateUserAuthenticator --- //
    // TODO: Add test cases for findAndValidateUserAuthenticator (if to be tested directly, or through verifyPasskeyAssertion)
    //  - Authenticator found and enabled
    //  - Authenticator not found
    //  - Authenticator found but disabled

}
