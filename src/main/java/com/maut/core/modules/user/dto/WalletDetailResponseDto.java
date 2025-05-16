package com.maut.core.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletDetailResponseDto {
    private UUID id;
    private LocalDateTime createdAt;
    private String address;
    private String displayName;
}
