package com.maut.core.modules.auth.dto;

import com.maut.core.modules.team.dto.TeamSummaryDto;
import com.maut.core.modules.user.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrentUserResponseDto {
    private UUID id;
    private UserType userType;
    private String firstName;
    private String lastName;
    private String email;
    private boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
    private TeamSummaryDto team;
    private Set<String> roles; // Adding roles as a set of strings for simplicity

    // If you need a more complex representation of roles, you can adjust this
    // For example, a Set<AdminRoleSummaryDto> if you create such a DTO.
}
