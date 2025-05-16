package com.maut.core.modules.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MautUserDetailResponseDto {
    private UUID id;
    private UUID mautUserId;
    private String clientSystemUserId;
    private String clientId; // From MautUser.clientApplication.clientId
    private LocalDateTime createdAt;
    private List<WalletDetailResponseDto> wallets;
    private List<AuthenticatorDetailResponseDto> authenticators;
}
