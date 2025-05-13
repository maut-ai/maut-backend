package com.maut.core.modules.clientapplication.controller;

import com.maut.core.modules.clientapplication.dto.CreateClientApplicationRequest;
import com.maut.core.modules.clientapplication.dto.ClientApplicationDetailResponse;
import com.maut.core.modules.clientapplication.dto.MyClientApplicationResponse;
import com.maut.core.modules.clientapplication.service.ClientApplicationService;
import com.maut.core.modules.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/clientapplication")
@RequiredArgsConstructor
@Slf4j
public class ClientApplicationController {

    private final ClientApplicationService clientApplicationService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ClientApplicationDetailResponse> createClientApplication(
            @Valid @RequestBody CreateClientApplicationRequest request,
            @AuthenticationPrincipal User authenticatedUser) {
        log.info("User {} attempting to create client application with name: {}", authenticatedUser.getEmail(), request.getName());
        try {
            ClientApplicationDetailResponse response = clientApplicationService.createClientApplication(request, authenticatedUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create client application for user {}: {}", authenticatedUser.getEmail(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while creating client application for user {}: {}", authenticatedUser.getEmail(), e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", e);
        }
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MyClientApplicationResponse>> listClientApplications(
            @AuthenticationPrincipal User authenticatedUser) {
        log.info("User {} requesting to list their client applications.", authenticatedUser.getEmail());
        List<MyClientApplicationResponse> response = clientApplicationService.listClientApplicationsForUser(authenticatedUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{clientApplicationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ClientApplicationDetailResponse> getClientApplicationDetails(
            @PathVariable UUID clientApplicationId,
            @AuthenticationPrincipal User authenticatedUser) {
        log.info("User {} requesting details for client application ID: {}", authenticatedUser.getEmail(), clientApplicationId);
        ClientApplicationDetailResponse response = clientApplicationService.getClientApplicationDetails(clientApplicationId, authenticatedUser);
        return ResponseEntity.ok(response);
    }
}
