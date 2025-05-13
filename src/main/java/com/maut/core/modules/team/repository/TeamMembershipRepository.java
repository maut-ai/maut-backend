package com.maut.core.modules.team.repository;

import com.maut.core.modules.team.model.TeamMembership;
import com.maut.core.modules.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TeamMembershipRepository extends JpaRepository<TeamMembership, UUID> {
    // Add custom query methods if needed, e.g., find by user and team
    // Optional<TeamMembership> findByUserAndTeam(User user, Team team);

    List<TeamMembership> findByUser(User user);
}
