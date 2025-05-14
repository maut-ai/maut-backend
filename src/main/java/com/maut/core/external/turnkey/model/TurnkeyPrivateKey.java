package com.maut.core.external.turnkey.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TurnkeyPrivateKey {
    private String privateKeyId; // Renamed from id
    private String privateKeyName;
    private String address; // Wallet address
    // Removed subOrganizationId as it's contextual to the service call
    // Add any other relevant fields returned by Turnkey for a private key
}
