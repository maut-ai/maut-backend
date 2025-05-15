package com.maut.core.modules.user.service;

import com.maut.core.modules.team.model.Team;
import com.maut.core.modules.team.service.TeamService;
import com.maut.core.modules.user.dto.MautUserResponseItemDTO;
import com.maut.core.modules.user.dto.PaginatedMautUsersResponseDTO;
import com.maut.core.modules.user.model.MautUser;
import com.maut.core.modules.user.model.User;
import com.maut.core.modules.user.repository.MautUserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MautUserService {

    private static final Logger log = LoggerFactory.getLogger(MautUserService.class);
    private final MautUserRepository mautUserRepository;
    private final TeamService teamService; // Added TeamService dependency

    // We will add the method to get paginated MautUsers by teamId here later.
    // For now, the main question is how to get the teamId for the logged-in User.

    @Transactional(readOnly = true)
    public PaginatedMautUsersResponseDTO getMautUsersForCurrentUserTeam(int offset, int limit) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            log.warn("User not authenticated or authentication principal is not of type User");
            // Depending on requirements, could throw an exception or return empty/error DTO
            return PaginatedMautUsersResponseDTO.builder().data(List.of()).recordsTotal(0).position(0).build();
        }

        User currentUser = (User) authentication.getPrincipal();
        log.debug("Fetching MautUsers for team of user: {}", currentUser.getEmail());

        Optional<Team> teamOptional = teamService.getTeamByOwner(currentUser);

        if (teamOptional.isEmpty()) {
            log.warn("No team found for user: {}", currentUser.getEmail());
            return PaginatedMautUsersResponseDTO.builder().data(List.of()).recordsTotal(0).position(0).build();
        }

        Team team = teamOptional.get();
        Pageable pageable = PageRequest.of(offset / limit, limit); // Spring PageRequest is 0-indexed for page number

        Page<MautUser> mautUserPage = mautUserRepository.findByTeamId(team.getId(), pageable);

        List<MautUserResponseItemDTO> dtoList = mautUserPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        long calculatedPosition = (long) offset + dtoList.size();

        return PaginatedMautUsersResponseDTO.builder()
                .data(dtoList)
                .recordsTotal(mautUserPage.getTotalElements())
                .position(calculatedPosition)
                .build();
    }

    private MautUserResponseItemDTO convertToDto(MautUser mautUser) {
        return MautUserResponseItemDTO.builder()
                .id(mautUser.getId())
                .mautUserId(mautUser.getMautUserId())
                .clientSystemUserId(mautUser.getClientSystemUserId())
                .clientId(mautUser.getClientApplication() != null ? mautUser.getClientApplication().getMautApiClientId() : null)
                .createdAt(mautUser.getCreatedAt())
                .build();
    }

}
