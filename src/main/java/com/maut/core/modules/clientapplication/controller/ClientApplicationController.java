package com.maut.core.modules.clientapplication.controller;

import com.maut.core.modules.clientapplication.dto.MyClientApplicationResponse;
import com.maut.core.modules.clientapplication.service.ClientApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/client-applications") // Using /api/v1 prefix
@RequiredArgsConstructor
@Slf4j
public class ClientApplicationController {

    private final ClientApplicationService clientApplicationService;

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('CLIENT')")
    public ResponseEntity<MyClientApplicationResponse> getMyClientApplications() {
        log.info("Received request to get current user's client applications.");
        MyClientApplicationResponse response = clientApplicationService.getMyApplications();
        return ResponseEntity.ok(response);
    }
}
