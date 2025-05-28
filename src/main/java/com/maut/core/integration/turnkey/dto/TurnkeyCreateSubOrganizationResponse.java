// In com.maut.core.integration.turnkey.dto.TurnkeyCreateSubOrganizationResponse.java
package com.maut.core.integration.turnkey.dto;

import lombok.Builder;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Data
@Builder
public class TurnkeyCreateSubOrganizationResponse {
    private boolean success;
    private String subOrganizationId;
    private String rootUserId; // The ID of the user created within the sub-org
    private String passkeyAuthenticatorId; // The ID of the registered passkey
    private String userPrivateKeyId; // The ID of the private key tied to the user's passkey
    private String userPrivateKeyAddress; // The address of the user's private key
    private String errorMessage;

    // --- Nested DTOs for parsing the full Turnkey response --- 

    /**
     * Models the "wallet" object within the createSubOrganizationResultV7.
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WalletResult {
        @JsonProperty("walletId")
        private String walletId;

        @JsonProperty("addresses")
        private List<String> addresses;
    }

    /**
     * Models the "createSubOrganizationResultV7" object from Turnkey's response.
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CreateSubOrganizationResultV7 {
        @JsonProperty("subOrganizationId")
        private String subOrganizationId;

        @JsonProperty("wallet")
        private WalletResult wallet;

        @JsonProperty("rootUserIds")
        private List<String> rootUserIds;
        
        // The API response also includes privateKeyIds for each root user's authenticator.
        // We'll need to figure out how that's structured if we need to capture it directly here.
        // For now, assuming the primary user's key ID/address might come from other fields or a separate call.
    }

    // --- DTOs for parsing the 'intent' within the nested activity response ---

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ResponseIntent_WalletAccountParams {
        @JsonProperty("curve")
        private String curve;
        @JsonProperty("pathFormat")
        private String pathFormat;
        @JsonProperty("path")
        private String path;
        @JsonProperty("addressFormat")
        private String addressFormat;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ResponseIntent_WalletParams {
        @JsonProperty("walletName")
        private String walletName;
        @JsonProperty("accounts")
        private List<ResponseIntent_WalletAccountParams> accounts;
        @JsonProperty("mnemonicLength")
        private Integer mnemonicLength;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ResponseIntent_AttestationParams {
        @JsonProperty("credentialId")
        private String credentialId;
        @JsonProperty("clientDataJson")
        private String clientDataJson;
        @JsonProperty("attestationObject")
        private String attestationObject;
        @JsonProperty("transports")
        private List<String> transports;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ResponseIntent_AuthenticatorParams {
        @JsonProperty("authenticatorName")
        private String authenticatorName;
        @JsonProperty("challenge")
        private String challenge;
        @JsonProperty("attestation")
        private ResponseIntent_AttestationParams attestation;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ResponseIntent_ApiKeyParams {
        @JsonProperty("apiKeyName")
        private String apiKeyName;
        @JsonProperty("publicKey")
        private String publicKey;
        @JsonProperty("curveType")
        private String curveType;
        @JsonProperty("expirationSeconds")
        private String expirationSeconds;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ResponseIntent_OauthProviderParams {
        @JsonProperty("providerName")
        private String providerName;
        @JsonProperty("oidcToken")
        private String oidcToken;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ResponseIntent_RootUserParams {
        @JsonProperty("userName")
        private String userName;
        @JsonProperty("userEmail")
        private String userEmail;
        @JsonProperty("userPhoneNumber")
        private String userPhoneNumber;
        @JsonProperty("apiKeys")
        private List<ResponseIntent_ApiKeyParams> apiKeys;
        @JsonProperty("authenticators")
        private List<ResponseIntent_AuthenticatorParams> authenticators;
        @JsonProperty("oauthProviders")
        private List<ResponseIntent_OauthProviderParams> oauthProviders;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ResponseIntent_CreateSubOrganizationIntentV7 {
        @JsonProperty("subOrganizationName")
        private String subOrganizationName;
        @JsonProperty("rootUsers")
        private List<ResponseIntent_RootUserParams> rootUsers;
        @JsonProperty("rootQuorumThreshold")
        private Integer rootQuorumThreshold;
        @JsonProperty("wallet")
        private ResponseIntent_WalletParams wallet;
        @JsonProperty("disableEmailRecovery")
        private Boolean disableEmailRecovery;
        @JsonProperty("disableEmailAuth")
        private Boolean disableEmailAuth;
        @JsonProperty("disableSmsAuth")
        private Boolean disableSmsAuth;
        @JsonProperty("disableOtpEmailAuth")
        private Boolean disableOtpEmailAuth;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CreateSubOrganizationIntentV7Holder {
        @JsonProperty("createSubOrganizationIntentV7")
        private ResponseIntent_CreateSubOrganizationIntentV7 createSubOrganizationIntentV7;
    }

    /**
     * Models the "result" object within the inner "activity" object, 
     * which directly contains the specific activity result (e.g., createSubOrganizationResultV7).
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ActivitySpecificResultHolder {
        // This field name needs to match exactly what Turnkey returns for this activity type.
        // For sub-organization creation, it's "createSubOrganizationResultV7".
        // For other activities (like create_private_keys), this field name would be different.
        // Using @JsonAnySetter or a Map could make this more generic if needed, but explicit is fine for now.
        @JsonProperty("createSubOrganizationResultV7")
        private CreateSubOrganizationResultV7 createSubOrganizationResultV7;

        // If parsing other activity types, you would add other fields like:
        // @JsonProperty("createPrivateKeysResultV2")
        // private CreatePrivateKeysResultV2 createPrivateKeysResultV2;
    }

    /**
     * Models the inner "activity" object found within the top-level "result".
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InnerActivityResult {
        @JsonProperty("type")
        private String type;

        // The "intent" object can be complex. If its details are not immediately needed,
        // it can be mapped to Object.class or a more specific DTO if parsed.
        @JsonProperty("intent")
        private CreateSubOrganizationIntentV7Holder intent;

        @JsonProperty("result")
        private ActivitySpecificResultHolder result;
    }

    /**
     * Models the "result" object which is a direct child of the main "activity" object.
     * It primarily contains another nested "activity" object.
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OverallActivityResult {
        @JsonProperty("activity")
        private InnerActivityResult activity;
    }

    /**
     * This is the top-level wrapper for a Turnkey activity response.
     * RestTemplate will deserialize the entire JSON into this class.
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TurnkeyActivityResponseWrapper {
        @JsonProperty("activity")
        private ActivityDetails activity;
    }

    /**
     * Models the main "activity" object in a Turnkey response, including its metadata.
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ActivityDetails {
        @JsonProperty("id")
        private String id;

        @JsonProperty("status")
        private String status;

        @JsonProperty("type")
        private String type;

        @JsonProperty("organizationId")
        private String organizationId;

        @JsonProperty("timestampMs")
        private String timestampMs;

        @JsonProperty("result")
        private OverallActivityResult result;
    }
}