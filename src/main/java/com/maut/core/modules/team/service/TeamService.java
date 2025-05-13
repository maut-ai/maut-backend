package com.maut.core.modules.team.service;

import com.maut.core.modules.team.model.Team;
import com.maut.core.modules.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;

    /**
     * Checks if a team with the given name already exists.
     *
     * @param teamName The name of the team to check.
     * @return true if a team with the name exists, false otherwise.
     */
    public boolean teamExistsByName(String teamName) {
        return teamRepository.existsByName(teamName);
    }

    /**
     * Creates and saves a new team.
     *
     * @param team The team object to create.
     * @return The saved Team entity.
     */
    @Transactional
    public Team createTeam(Team team) {
        // Potential future logic: Validate team name uniqueness before saving
        // Optional<Team> existingTeam = teamRepository.findByName(team.getName());
        // if (existingTeam.isPresent()) {
        //     throw new DataIntegrityViolationException("Team name already exists: " + team.getName());
        // }
        return teamRepository.save(team);
    }

    // Other service methods will be added here
}
