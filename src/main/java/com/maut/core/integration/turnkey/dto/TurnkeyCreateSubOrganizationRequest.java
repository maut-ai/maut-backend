package com.maut.core.integration.turnkey.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

/**
 * Represents the "parameters" field for a CREATE_SUB_ORGANIZATION_V7 Turnkey API call.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // Important to omit null fields from JSON
public class TurnkeyCreateSubOrganizationRequest {

    @JsonProperty("subOrganizationName")
    private String subOrganizationName;

    @JsonProperty("rootUsers")
    @Singular // For easy building of the list
    private List<RootUserParams> rootUsers;

    @JsonProperty("rootQuorumThreshold")
    private Integer rootQuorumThreshold; // Optional: if not set, Turnkey uses a default (likely 1)

    @JsonProperty("wallet")
    private WalletParams wallet; // Optional: defines initial wallet and keys

    // Optional boolean flags (default to false if not sent, but Turnkey might have its own defaults)
    @JsonProperty("disableEmailRecovery")
    private Boolean disableEmailRecovery;

    @JsonProperty("disableEmailAuth")
    private Boolean disableEmailAuth;

    @JsonProperty("disableSmsAuth")
    private Boolean disableSmsAuth;

    @JsonProperty("disableOtpEmailAuth")
    private Boolean disableOtpEmailAuth;

    // --- Nested DTOs --- 

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RootUserParams {
        @JsonProperty("userName")
        private String userName;

        // userEmail, userPhoneNumber, apiKeys, oauthProviders can be added if needed
        // For passkey-only setup, only authenticators are critical initially.

        @JsonProperty("authenticators")
        @Singular
        private List<AuthenticatorParams> authenticators;

        @JsonProperty("apiKeys")
        @Singular
        private List<ApiKeyParams> apiKeys;

        @JsonProperty("oauthProviders")
        @Singular
        private List<OauthProviderParams> oauthProviders;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AuthenticatorParams {
        @JsonProperty("authenticatorName")
        private String authenticatorName;

        @JsonProperty("challenge")
        private String challenge; // The challenge sent to navigator.credentials.create()

        @JsonProperty("attestation")
        private AttestationParams attestation; // The PublicKeyCredential result from navigator.credentials.create()
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AttestationParams {
        @JsonProperty("credentialId")
        private String credentialId; // from PublicKeyCredential.id (base64url of rawId)

        @JsonProperty("clientDataJson")
        private String clientDataJson; // from AuthenticatorAttestationResponse.clientDataJSON (base64url of ArrayBuffer)

        @JsonProperty("attestationObject")
        private String attestationObject; // from AuthenticatorAttestationResponse.attestationObject (base64url of ArrayBuffer)

        @JsonProperty("transports")
        @Singular
        private List<String> transports; // e.g., ["AUTHENTICATOR_TRANSPORT_INTERNAL"], from PublicKeyCredential.response.getTransports()
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WalletParams {
        @JsonProperty("walletName")
        private String walletName;

        @JsonProperty("accounts")
        @Singular
        private List<WalletAccountParams> accounts;

        // mnemonicLength can be added if needed
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WalletAccountParams {
        @JsonProperty("curve")
        private String curve; // e.g., "CURVE_SECP256K1", "CURVE_ED25519"

        @JsonProperty("pathFormat")
        private String pathFormat; // e.g., "PATH_FORMAT_BIP32"

        @JsonProperty("path")
        private String path; // e.g., "m/44'/60'/0'/0/0"

        @JsonProperty("addressFormat")
        private String addressFormat; // e.g., "ADDRESS_FORMAT_ETHEREUM"
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ApiKeyParams {
        @JsonProperty("apiKeyName")
        private String apiKeyName;

        @JsonProperty("publicKey")
        private String publicKey; // The public key hex string

        @JsonProperty("curveType")
        private String curveType; // e.g., "API_KEY_CURVE_P256"

        @JsonProperty("expirationSeconds")
        private String expirationSeconds; // Optional
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OauthProviderParams {
        @JsonProperty("providerName")
        private String providerName;

        @JsonProperty("oidcToken")
        private String oidcToken; // Base64 encoded OIDC token
    }
    
}