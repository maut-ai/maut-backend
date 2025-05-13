package com.maut.core.modules.role.dto;

import com.maut.core.modules.role.model.AdminRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminRoleResponse {

    private UUID id;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AdminRoleResponse fromEntity(AdminRole adminRole) {
        if (adminRole == null) {
            return null;
        }
        return AdminRoleResponse.builder()
                .id(adminRole.getId())
                .name(adminRole.getName())
                .createdAt(adminRole.getCreatedAt() != null ? LocalDateTime.ofInstant(adminRole.getCreatedAt(), ZoneOffset.UTC) : null)
                .updatedAt(adminRole.getUpdatedAt() != null ? LocalDateTime.ofInstant(adminRole.getUpdatedAt(), ZoneOffset.UTC) : null)
                .build();
    }
}
