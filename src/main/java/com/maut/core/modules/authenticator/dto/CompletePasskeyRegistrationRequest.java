package com.maut.core.modules.authenticator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Map;

/**
 * DTO for completing passkey registration.
 * Based on WebAuthn `PublicKeyCredentialWithAttestationJSON` structure from browser.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompletePasskeyRegistrationRequest {

    @NotNull(message = "Turnkey attestation data cannot be null.")
    @NotEmpty(message = "Turnkey attestation data cannot be empty.")
    private Map<String, Object> turnkeyAttestation; // This is the PublicKeyCredential as JSON from the client

    @NotEmpty(message = "Turnkey challenge cannot be empty.")
    private String turnkeyChallenge; // This is the challenge received from initiatePasskeyRegistration

    @NotEmpty(message = "Client data JSON cannot be empty.")
    private String clientDataJSON; // Base64URL encoded clientDataJSON

    // Transports can be optional or have specific validation if required
    private String[] transports;

    @Size(max = 255, message = "Authenticator name cannot exceed 255 characters.")
    private String authenticatorName;

}
