package com.maut.core.modules.authenticator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompletePasskeyRegistrationResponse {

    private String status;                // e.g., "SUCCESS", "FAILURE"
    private String authenticatorId;       // Maut Authenticator ID (UUID as String)
    private String turnkeyAuthenticatorId; // Turnkey's Authenticator ID
    private String message;               // Optional message, e.g., on failure

}
