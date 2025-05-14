package com.maut.core.external.turnkey.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor // For Jackson deserialization
public class PrivateKeyDetails {
    private String privateKeyId;
    private String privateKeyName;
    private String status; // e.g., "PRIVATE_KEY_STATUS_ACTIVE"
    private String algorithm;
    private String curve;
    private String publicKey; // Public key corresponding to the private key
    private List<Address> addresses;
    private List<String> tags;
}
