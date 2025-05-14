package com.maut.core.modules.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollWalletResponse {

    private String walletId;
    private String walletAddress;

}
