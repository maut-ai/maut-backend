package com.maut.core.modules.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InitiateSigningRequest {

    @NotBlank(message = "Transaction type cannot be blank.")
    private String transactionType;

    @NotNull(message = "Transaction details cannot be null.")
    private Map<String, Object> transactionDetails; // Represents the Turnkey transaction object structure

    private String turnkeyPolicyId; // Optional: If not provided, wallet's default policy might be used

}
