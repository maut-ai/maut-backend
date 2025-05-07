package com.maut.core.modules.wallet.dto;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class EnrollWalletRequest {

    @Size(max = 255, message = "Wallet display name must be less than 255 characters")
    private String walletDisplayName;

}
