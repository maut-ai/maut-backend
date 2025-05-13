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
