package com.maut.core.modules.authenticator.dto.webauthn;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PasskeyRegistrationResultDto {

    private boolean success;
    private String credentialId; // Base64URL encoded credential ID, if successful
    private String friendlyName; // Optional, user-provided or derived name for the credential
    private String createdAt;    // ISO-8601 formatted timestamp when the credential was created
    private String message; // Optional, for errors or additional info

}
