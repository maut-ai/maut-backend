package com.maut.core.integration.turnkey.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TurnkeyFinalizePasskeyRegistrationResponse {
    private boolean success;
    // The unique ID of the authenticator (passkey) newly created in Turnkey
    private String turnkeyAuthenticatorId;
    // The public key of the newly registered credential (optional, for reference)
    private String publicKey;
    // Any error message if success is false
    private String errorMessage;
}
