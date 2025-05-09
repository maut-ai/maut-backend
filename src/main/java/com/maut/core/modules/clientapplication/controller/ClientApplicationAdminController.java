package com.maut.core.modules.clientapplication.controller;

import com.maut.core.modules.clientapplication.dto.CreateClientApplicationRequest;
import com.maut.core.modules.clientapplication.dto.CreateClientApplicationResponse;
import com.maut.core.modules.clientapplication.service.ClientApplicationAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/admin/client-applications") // Admin-specific path
@RequiredArgsConstructor
@Slf4j
public class ClientApplicationAdminController {

    private final ClientApplicationAdminService clientApplicationAdminService;

    @PostMapping
    public ResponseEntity<CreateClientApplicationResponse> createClientApplication(
            @Valid @RequestBody CreateClientApplicationRequest request) {
        log.info("Received request to create client application with name: {}", request.getClientName());
        try {
            CreateClientApplicationResponse response = clientApplicationAdminService.createClientApplication(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create client application due to bad request: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while creating client application: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", e);
        }
    }
}
