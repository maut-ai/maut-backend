package com.maut.core.modules.clientapplication.service;

import com.maut.core.modules.clientapplication.dto.CreateClientApplicationRequest;
import com.maut.core.modules.clientapplication.dto.ClientApplicationDetailResponse;
import com.maut.core.modules.clientapplication.dto.MyClientApplicationResponse;
import com.maut.core.modules.clientapplication.model.ClientApplication;
import com.maut.core.modules.clientapplication.repository.ClientApplicationRepository;
import com.maut.core.modules.team.model.Team;
import com.maut.core.modules.team.repository.TeamRepository;
import com.maut.core.modules.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientApplicationServiceImpl implements ClientApplicationService {

    private final ClientApplicationRepository clientApplicationRepository;
    private final TeamRepository teamRepository;

    @Override
    public ClientApplicationDetailResponse createClientApplication(
            CreateClientApplicationRequest request, User authenticatedUser) {
        log.info("User {} creating client application with name: {}", authenticatedUser.getEmail(), request.getName());

        Team team = teamRepository.findByOwner(authenticatedUser)
                .orElseThrow(() -> {
                    log.warn("User {} is not an owner of any team. Cannot create client application.", authenticatedUser.getEmail());
                    return new IllegalArgumentException("User must be an owner of a team to create a client application.");
                });

        ClientApplication clientApplication = new ClientApplication();
        clientApplication.setName(request.getName());
        clientApplication.setTeam(team);
        clientApplication.setMautApiClientId("ma_id_" + UUID.randomUUID().toString().replace("-", ""));
        clientApplication.setClientSecret(UUID.randomUUID().toString().replace("-", "")); // Added clientSecret
        clientApplication.setEnabled(true);

        ClientApplication savedApp = clientApplicationRepository.save(clientApplication);
        log.info("Successfully created client application ID {} for user {} (team ID {})", 
                 savedApp.getId(), authenticatedUser.getEmail(), team.getId());
        return convertToClientApplicationDetailResponse(savedApp);
    }

    @Override
    public List<MyClientApplicationResponse> listClientApplicationsForUser(User authenticatedUser) {
        log.info("User {} listing their client applications.", authenticatedUser.getEmail());
        Team team = teamRepository.findByOwner(authenticatedUser)
                .orElseThrow(() -> {
                    log.warn("User {} is not an owner of any team. Cannot list applications.", authenticatedUser.getEmail());
                    // Depending on requirements, could return empty list or throw error
                    return new IllegalArgumentException("User must be an owner of a team to list client applications.");
                });

        List<ClientApplication> applications = clientApplicationRepository.findByTeamId(team.getId());
        return applications.stream()
                .map(this::convertToMyClientApplicationResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ClientApplicationDetailResponse getClientApplicationDetails(
            UUID clientApplicationId, User authenticatedUser) {
        log.info("User {} requesting details for client application ID: {}", authenticatedUser.getEmail(), clientApplicationId);
        Team team = teamRepository.findByOwner(authenticatedUser)
                .orElseThrow(() -> {
                    log.warn("User {} is not an owner of any team. Cannot get application details.", authenticatedUser.getEmail());
                    return new IllegalArgumentException("User must be an owner of a team to view client application details.");
                });

        ClientApplication clientApplication = clientApplicationRepository.findById(clientApplicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client application not found."));

        if (!clientApplication.getTeam().getId().equals(team.getId())) {
            log.warn("User {} (team ID {}) attempted to access client application ID {} owned by team ID {}. Access denied.", 
                     authenticatedUser.getEmail(), team.getId(), clientApplicationId, clientApplication.getTeam().getId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this client application.");
        }

        return convertToClientApplicationDetailResponse(clientApplication);
    }

    private MyClientApplicationResponse convertToMyClientApplicationResponse(ClientApplication app) {
        return MyClientApplicationResponse.builder()
                .id(app.getId())
                .name(app.getName())
                .mautApiClientId(app.getMautApiClientId())
                .createdAt(app.getCreatedAt())
                .enabled(app.isEnabled())
                .build();
    }

    private ClientApplicationDetailResponse convertToClientApplicationDetailResponse(ClientApplication app) {
        return ClientApplicationDetailResponse.builder()
                .id(app.getId())
                .name(app.getName())
                .mautApiClientId(app.getMautApiClientId())
                .createdAt(app.getCreatedAt())
                .updatedAt(app.getUpdatedAt())
                .enabled(app.isEnabled())
                .allowedOrigins(app.getAllowedOrigins())
                .teamId(app.getTeam().getId())
                .build();
    }
}
