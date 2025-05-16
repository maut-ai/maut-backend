package com.maut.core.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticatorDetailResponseDto {
    private UUID id;
    private String friendlyName;
    private String type; // e.g., "Passkey"
    private OffsetDateTime createdAt;
    private Instant lastUsedAt;
}
