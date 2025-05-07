package com.maut.core.modules.authenticator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyPasskeyAssertionRequest {

    @NotBlank(message = "Passkey credential ID is required.")
    private String credentialId;

    @NotNull(message = "Turnkey assertion data is required.")
    private Map<String, Object> turnkeyAssertion; // Flexible map to hold assertion data from Turnkey/frontend
    // This map would typically contain fields like:
    // - clientDataJSON
    // - authenticatorData
    // - signature
    // - userHandle (optional, could be derived or validated against MautUser)
    // - challenge (important for replay protection, should be from a preceding step)
}
