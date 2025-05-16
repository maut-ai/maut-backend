package com.maut.core.modules.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletDetailsResponse {

    private String walletId; // UUID of UserWallet
    private String displayName;
    private String walletAddress;
    private String turnkeySubOrganizationId;
    private String turnkeyMautPrivateKeyId;
    private String turnkeyUserPrivateKeyId;
    private Map<String, Object> currentPolicy; // Placeholder for policy details
    private Instant createdAt;

}
