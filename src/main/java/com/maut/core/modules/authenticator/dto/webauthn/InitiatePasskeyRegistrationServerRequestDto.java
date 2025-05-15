package com.maut.core.modules.authenticator.dto.webauthn;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Optional request body for initiating passkey registration.
 * Allows specifying preferences like authenticator attachment.
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InitiatePasskeyRegistrationServerRequestDto {

    /**
     * Preference for authenticator attachment (e.g., "platform" or "cross-platform").
     * If null, the server may use a default (e.g., "platform").
     */
    private String authenticatorAttachmentPreference;

}
