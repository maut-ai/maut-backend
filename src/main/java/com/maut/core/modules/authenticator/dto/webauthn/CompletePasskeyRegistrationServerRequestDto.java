package com.maut.core.modules.authenticator.dto.webauthn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for the client to send the authenticator's attestation response
 * to the server to complete passkey registration.
 * Based on PublicKeyCredential structure from WebAuthn client-side.
 */
@Data
@NoArgsConstructor
public class CompletePasskeyRegistrationServerRequestDto {

    private String challengeId; // The challenge originally sent to the client
    private String id; // Base64URL encoded credential ID
    private String rawId; // Base64URL encoded credential ID (same as id, but some libraries distinguish)
    private AuthenticatorAttestationResponseDto response;
    private String type; // Should be "public-key"
    // private Object clientExtensionResults; // For future use

    /**
     * Represents the AuthenticatorAttestationResponse part of the PublicKeyCredential.
     */
    @Data
    @NoArgsConstructor
    public static class AuthenticatorAttestationResponseDto {
        private String clientDataJSON; // Base64URL encoded client data JSON
        private String attestationObject; // Base64URL encoded attestation object
        // private List<String> transports; // Only available on getAssertion, not create

        @JsonCreator
        public AuthenticatorAttestationResponseDto(
                @JsonProperty("clientDataJSON") String clientDataJSON,
                @JsonProperty("attestationObject") String attestationObject) {
            this.clientDataJSON = clientDataJSON;
            this.attestationObject = attestationObject;
        }
    }

    @JsonCreator
    public CompletePasskeyRegistrationServerRequestDto(
            @JsonProperty("challengeId") String challengeId,
            @JsonProperty("id") String id,
            @JsonProperty("rawId") String rawId,
            @JsonProperty("response") AuthenticatorAttestationResponseDto response,
            @JsonProperty("type") String type) {
        this.challengeId = challengeId;
        this.id = id;
        this.rawId = rawId;
        this.response = response;
        this.type = type;
    }
}
