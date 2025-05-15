package com.maut.core.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MautUserResponseItemDTO {
    private UUID id;
    private UUID mautUserId;
    private String clientSystemUserId;
    private String clientId;
    private LocalDateTime createdAt;
}
