package com.maut.core.integration.turnkey.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TurnkeyInitiatePasskeyRegistrationResponse {
    // The challenge that the client (browser WebAuthn API) needs to sign
    private String challenge;
    // The Turnkey-specific ID for this registration attempt or pre-created authenticator object
    private String turnkeyAttestationId; // Or similar, depends on Turnkey's flow
    // Public Key Credential Creation Options (as a JSON string or a structured object)
    // This would typically be passed directly to navigator.credentials.create()
    private String publicKeyCredentialCreationOptions;
    // Any other data from Turnkey needed to continue the flow
}
