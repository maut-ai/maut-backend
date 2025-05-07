package com.maut.core.modules.authenticator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyPasskeyAssertionResponse {

    private boolean verified;
    private String authenticatorId; // The ID of the UserAuthenticator record
    private String message;

}
