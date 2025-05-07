package com.maut.core.modules.authenticator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maut.core.modules.authenticator.dto.CompletePasskeyRegistrationRequest;
import com.maut.core.modules.authenticator.dto.CompletePasskeyRegistrationResponse;
import com.maut.core.modules.authenticator.dto.InitiatePasskeyRegistrationResponse;
import com.maut.core.modules.authenticator.dto.ListPasskeysResponse;
import com.maut.core.modules.authenticator.dto.PasskeyListItem;
import com.maut.core.modules.authenticator.service.AuthenticatorService;
import com.maut.core.modules.user.model.MautUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticatorController.class)
public class AuthenticatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticatorService authenticatorService;

    @Autowired
    private ObjectMapper objectMapper;

    private MautUser testMautUser;

    @BeforeEach
    void setUp() {
        testMautUser = new MautUser();
        testMautUser.setId(UUID.randomUUID());
        // Note: In a real scenario with Spring Security, user would be injected via @AuthenticationPrincipal
        // For WebMvcTest, we often test controller logic assuming authentication has occurred
        // or mock the security context if testing security-specific aspects.
    }

    @Test
    void initiatePasskeyRegistration_shouldReturnInitiationResponse() throws Exception {
        InitiatePasskeyRegistrationResponse mockResponse = InitiatePasskeyRegistrationResponse.builder()
                .turnkeyChallenge("test-challenge")
                .turnkeyAttestationRequest(Collections.singletonMap("rp", Collections.singletonMap("id", "test-rp-id")))
                .build();

        // The controller currently passes null for MautUser, reflecting current implementation
        when(authenticatorService.initiatePasskeyRegistration(null)).thenReturn(mockResponse);

        mockMvc.perform(post("/v1/authenticators/register/initiate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.turnkeyChallenge").value("test-challenge"))
                .andExpect(jsonPath("$.turnkeyAttestationRequest.rp.id").value("test-rp-id"));
    }

    @Test
    void completePasskeyRegistration_shouldReturnCompletionResponse() throws Exception {
        Map<String, Object> sampleAttestation = new HashMap<>();
        sampleAttestation.put("credentialId", "test-credential-id");
        sampleAttestation.put("clientDataJSON", "test-client-data-json");
        // Add other necessary fields expected by Turnkey within the attestation map

        CompletePasskeyRegistrationRequest request = CompletePasskeyRegistrationRequest.builder()
            .turnkeyAttestation(sampleAttestation)
            .authenticatorName("My Test Passkey")
            .build();
        CompletePasskeyRegistrationResponse mockResponse = CompletePasskeyRegistrationResponse.builder()
                .authenticatorId(UUID.randomUUID().toString())
                .turnkeyAuthenticatorId("turnkey-auth-id")
                .status("SUCCESS")
                .build();
        
        // The controller currently passes null for MautUser
        when(authenticatorService.completePasskeyRegistration(null, any(CompletePasskeyRegistrationRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/v1/authenticators/register/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()) // Expecting 201 CREATED
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.authenticatorId").exists());
    }

    @Test
    void listPasskeys_shouldReturnListOfPasskeys() throws Exception {
        PasskeyListItem passkeyItem = PasskeyListItem.builder()
                .id(UUID.randomUUID().toString())
                .name("Test Passkey")
                .credentialId("cred-id")
                .createdAt(Instant.now())
                .lastUsedAt(Instant.now())
                .type("PLATFORM")
                .enabled(true)
                .build();
        List<PasskeyListItem> passkeys = Collections.singletonList(passkeyItem);
        ListPasskeysResponse mockResponse = ListPasskeysResponse.builder()
                .passkeys(passkeys)
                .limit(10)
                .offset(0)
                .totalPasskeys(1)
                .build();

        // The controller currently passes null for MautUser
        when(authenticatorService.listPasskeys(null, 10, 0)).thenReturn(mockResponse);

        mockMvc.perform(get("/v1/passkeys") // Path for listing passkeys
                        .param("limit", "10")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.passkeys[0].name").value("Test Passkey"))
                .andExpect(jsonPath("$.totalPasskeys").value(1));
    }

    @Test
    void deletePasskey_shouldReturnNoContent() throws Exception {
        String passkeyIdToDelete = UUID.randomUUID().toString();

        // The controller currently passes null for MautUser
        doNothing().when(authenticatorService).deletePasskey(null, passkeyIdToDelete);

        mockMvc.perform(delete("/v1/passkeys/" + passkeyIdToDelete)) // Path for deleting a passkey
                .andExpect(status().isNoContent()); // Expecting 204 No Content
    }
}
