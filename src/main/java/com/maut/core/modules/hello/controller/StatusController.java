package com.maut.core.modules.hello.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handling status/health check endpoints.
 * Part of the hello module within the monolithic application.
 * Following API versioning standard with v1 in the path.
 */
@RestController
@RequestMapping("/v1/status")
public class StatusController {

    /**
     * Simple health check endpoint.
     * @return 200 OK response with status information
     */
    @GetMapping
    public ResponseEntity<Object> getStatus() {
        return ResponseEntity.ok().body(
                new Status("UP", "Service is running normally")
        );
    }

    /**
     * Simple DTO for status response.
     */
    private record Status(String status, String message) {
    }
}
