package com.maut.core.modules.clientapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientApplicationDetailResponse {
    private UUID id;
    private String name;
    private String mautApiClientId;
    private String clientSecret;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean enabled;
    private List<String> allowedOrigins;
    private UUID teamId;
}
