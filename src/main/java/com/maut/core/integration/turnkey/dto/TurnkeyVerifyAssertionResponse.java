package com.maut.core.integration.turnkey.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TurnkeyVerifyAssertionResponse {
    private boolean success;
    // The Maut user ID associated with the successfully verified passkey
    private String mautUserId;
    // The Turnkey authenticator ID that was verified
    private String turnkeyAuthenticatorId;
    // Any error message if success is false
    private String errorMessage;
    // Optionally, updated counter values or other authenticator details if provided by Turnkey
}
