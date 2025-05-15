package com.maut.core.modules.authenticator.dto.webauthn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents the PublicKeyCredentialCreationOptions structure sent to the client
 * to initiate passkey registration. Based on the WebAuthn specification.
 * All binary data (e.g., challenge, user.id) should be Base64URL encoded strings.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PublicKeyCredentialCreationOptionsDto {

    @JsonProperty("rp")
    private RpDto relyingParty;

    private UserDto user;
    private String challenge; // Base64URL encoded

    @JsonProperty("pubKeyCredParams")
    private List<PubKeyCredParamDto> publicKeyCredentialParameters;

    private Long timeout;
    private List<ExcludeCredentialDto> excludeCredentials;
    private AuthenticatorSelectionCriteriaDto authenticatorSelection;
    private String attestation; // e.g., "none", "indirect", "direct"
    // private Object extensions; // For future use, if needed

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RpDto {
        private String id;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDto {
        private String id; // Base64URL encoded user handle (e.g., MautUser.id as UUID string -> bytes -> Base64URL)
        private String name;
        private String displayName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PubKeyCredParamDto {
        private String type; // e.g., "public-key"
        private Integer alg; // COSE algorithm identifier (e.g., -7 for ES256, -257 for RS256)
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthenticatorSelectionCriteriaDto {
        private String authenticatorAttachment; // e.g., "platform", "cross-platform"
        private Boolean requireResidentKey;
        private String userVerification; // e.g., "required", "preferred", "discouraged"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExcludeCredentialDto {
        private String type; // e.g., "public-key"
        private String id; // Base64URL encoded credential ID
        private List<String> transports; // e.g., ["internal", "usb", "nfc", "ble"]
    }
}
