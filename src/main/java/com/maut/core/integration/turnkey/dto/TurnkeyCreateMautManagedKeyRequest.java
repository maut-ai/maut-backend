// In com.maut.core.integration.turnkey.dto.TurnkeyCreateMautManagedKeyRequest.java
package com.maut.core.integration.turnkey.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

/**
 * Represents the parameters for a Turnkey CREATE_PRIVATE_KEYS_V2 activity.
 */
@Data
@Builder
public class TurnkeyCreateMautManagedKeyRequest {

    private String subOrganizationId; // The ID of the sub-organization to create the key(s) in.

    @Singular // Allows adding one private key at a time using builder().privateKey(params)
    private List<PrivateKeyParams> privateKeys;

    @Data
    @Builder
    public static class PrivateKeyParams {
        private String privateKeyName;
        private String curve; // e.g., "CURVE_SECP256K1", "CURVE_ED25519"
        
        @Singular("addressFormat") // Allows builder().addressFormat("format")
        private List<String> addressFormats; // e.g., ["ADDRESS_FORMAT_ETHEREUM", "ADDRESS_FORMAT_SOLANA"]
        
        @Singular("privateKeyTag") // Allows builder().privateKeyTag("tag")
        private List<String> privateKeyTags; // Optional: for categorization
    }
}