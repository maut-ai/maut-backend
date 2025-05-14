package com.maut.core.external.turnkey.model.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PrivateKeySpecification {
    private String privateKeyName;
    private String algorithm; // e.g., "ALGORITHM_ECDSA"
    private String curve;     // e.g., "CURVE_SECP256K1"
    // private String signingScheme; // Often defaults, or part of policy. For now, let's assume it's not directly in basic key creation spec.
    private List<String> tags;
}
