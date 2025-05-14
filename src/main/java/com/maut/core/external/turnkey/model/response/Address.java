package com.maut.core.external.turnkey.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // For Jackson deserialization
public class Address {
    private String format;    // e.g., "ADDRESS_FORMAT_ETHEREUM"
    private String address;   // e.g., "0x..."
    private String compressedPublicKey; // Often included
}
