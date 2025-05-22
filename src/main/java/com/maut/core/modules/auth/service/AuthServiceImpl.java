package com.maut.core.modules.auth.service;

import com.maut.core.modules.auth.dto.ClientRegistrationRequest;
import com.maut.core.common.exception.EmailAlreadyExistsException;
import com.maut.core.common.exception.TeamNameAlreadyExistsException;
import com.maut.core.modules.auth.exception.PasswordMismatchException;
import com.maut.core.modules.role.model.TeamRole;
import com.maut.core.modules.role.repository.TeamRoleRepository;
import com.maut.core.modules.team.model.Team;
import com.maut.core.modules.team.service.TeamMembershipService;
import com.maut.core.modules.team.service.TeamService;
import com.maut.core.modules.team.model.TeamMembership;
import com.maut.core.modules.user.model.User;
import com.maut.core.modules.user.enums.UserType;
import com.maut.core.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final TeamService teamService;
    private final TeamMembershipService teamMembershipService;
    private final TeamRoleRepository teamRoleRepository;

    private static final String OWNER_ROLE_NAME = "ROLE_OWNER";

    @Override
    @Transactional // Ensure atomicity
    public User registerClient(ClientRegistrationRequest request) throws PasswordMismatchException, EmailAlreadyExistsException, TeamNameAlreadyExistsException {
        log.info("Registering new client with email: {}", request.getEmail());

        // 1. Validate passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            log.warn("Password mismatch for email: {}", request.getEmail());
            throw new PasswordMismatchException("Passwords do not match");
        }

        // 2. Check if email already exists
        if (userService.existsByEmail(request.getEmail())) {
            log.warn("Email already exists: {}", request.getEmail());
            throw new EmailAlreadyExistsException("An account with this email already exists: " + request.getEmail());
        }

        // 3. Check if team name already exists
        if (teamService.teamExistsByName(request.getTeamName())) {
            log.warn("Team name already exists: {}", request.getTeamName());
            throw new TeamNameAlreadyExistsException("A team with this name already exists: " + request.getTeamName());
        }

        // 4. Hash password
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // 5. Create User
        User newUser = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(hashedPassword)
                .userType(UserType.CLIENT)
                .isActive(true)
                .build();
        User savedUser = userService.createUser(newUser);
        log.info("User created successfully with ID: {} for email: {}", savedUser.getId(), savedUser.getEmail());

        // 6. Create Team
        Team newTeam = Team.builder()
                .name(request.getTeamName())
                .owner(savedUser)
                .build();
        Team savedTeam = teamService.createTeam(newTeam);
        log.info("Team created successfully with ID: {} for owner: {}", savedTeam.getId(), savedUser.getId());

        // 7. Find Owner Role
        TeamRole ownerRole = teamRoleRepository.findByName(OWNER_ROLE_NAME)
                .orElseThrow(() -> {
                    log.error("{} role not found in database. Seeding might have failed.", OWNER_ROLE_NAME);
                    return new RuntimeException(OWNER_ROLE_NAME + " role not found");
                });

        // 8. Add User to Team as Owner
        TeamMembership ownerMembership = teamMembershipService.addTeamMember(savedUser, savedTeam, ownerRole);
        log.info("User {} assigned as owner of team {}. Membership ID: {}", savedUser.getEmail(), savedTeam.getName(), ownerMembership.getId());

        return savedUser;
    }
}
