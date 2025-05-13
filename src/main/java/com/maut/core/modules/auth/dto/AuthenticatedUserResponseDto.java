package com.maut.core.modules.auth.dto;

import com.maut.core.modules.team.dto.TeamMembershipInfoResponseDto;
import com.maut.core.modules.user.enums.UserType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class AuthenticatedUserResponseDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private UserType userType;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TeamMembershipInfoResponseDto> teamMemberships;
}
