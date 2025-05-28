// In com.maut.core.integration.turnkey.dto.TurnkeyCreateMautManagedKeyResponse.java
package com.maut.core.integration.turnkey.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Simplified user-facing response for creating a Maut-managed key.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TurnkeyCreateMautManagedKeyResponse {
    private boolean success;
    private String privateKeyId;
    private String privateKeyName;
    private String privateKeyAddress; // Typically the first address if multiple formats
    private String errorMessage;

    /**
     * Wrapper to deserialize the full Turnkey API activity response for CREATE_PRIVATE_KEYS_V2.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TurnkeyActivityResponseWrapper {
        private Activity activity;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Activity {
        private String id;
        private String status;
        private String type;
        private String organizationId;
        private ResultWrapper result;
        private String timestampMs;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResultWrapper {
        @JsonProperty("createPrivateKeysResultV2")
        private CreatePrivateKeysResultV2 createPrivateKeysResultV2;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatePrivateKeysResultV2 {
        private List<PrivateKeyDetails> privateKeys;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrivateKeyDetails {
        private String privateKeyId;
        private String privateKeyName;
        private List<AddressEntry> addresses;
        private List<String> tags;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressEntry {
        private String format;
        private String address;
    }
}