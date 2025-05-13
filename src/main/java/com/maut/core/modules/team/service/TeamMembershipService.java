package com.maut.core.modules.team.service;

import com.maut.core.modules.role.model.TeamRole;
import com.maut.core.modules.team.model.Team;
import com.maut.core.modules.team.model.TeamMembership;
import com.maut.core.modules.team.repository.TeamMembershipRepository;
import com.maut.core.modules.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamMembershipService {

    private final TeamMembershipRepository teamMembershipRepository;

    /**
     * Adds a user to a team with a specific role.
     *
     * @param user     The user to add.
     * @param team     The team to add the user to.
     * @param teamRole The role the user will have in the team.
     * @return The newly created TeamMembership entity.
     */
    @Transactional
    public TeamMembership addTeamMember(User user, Team team, TeamRole teamRole) {
        TeamMembership membership = TeamMembership.builder()
                .user(user)
                .team(team)
                .teamRole(teamRole)
                // joinedAt is handled by @CreationTimestamp
                .build();
        
        // Potential future logic: Check if membership already exists
        // Optional<TeamMembership> existing = teamMembershipRepository.findByUserAndTeam(user, team);
        // if (existing.isPresent()) { 
        //    // Handle update or throw exception 
        //    throw new IllegalStateException("User is already a member of this team.");
        // }

        return teamMembershipRepository.save(membership);
    }

    // Other service methods will be added here
}
