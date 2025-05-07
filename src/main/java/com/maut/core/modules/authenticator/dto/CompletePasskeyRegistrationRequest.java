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
    private Map<String, Object> turnkeyAttestation; // Or a more specific DTO if structure is known

    @Size(max = 255, message = "Authenticator name cannot exceed 255 characters.")
    private String authenticatorName;

}
