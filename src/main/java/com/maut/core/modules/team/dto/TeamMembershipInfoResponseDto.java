package com.maut.core.modules.team.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class TeamMembershipInfoResponseDto {
    private UUID teamId;
    private String teamName;
    private String userRoleName; // e.g., "ROLE_OWNER", "ROLE_MEMBER"
}
