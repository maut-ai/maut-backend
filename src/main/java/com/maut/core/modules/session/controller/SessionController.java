package com.maut.core.modules.session.controller;

import com.maut.core.modules.session.dto.SessionRequest; 
import com.maut.core.modules.session.dto.SessionResponse; 
import com.maut.core.modules.session.service.SessionService; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/session") 
public class SessionController {

    private final SessionService sessionService;

    @Autowired
    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public ResponseEntity<SessionResponse> createSession(@Valid @RequestBody SessionRequest sessionRequest) {
        try {
            SessionResponse sessionResponse = sessionService.processClientSession(sessionRequest);
            return ResponseEntity.ok(sessionResponse);
        } catch (SecurityException e) {
            // Consider structured logging instead of System.err for production
            System.err.println("SecurityException in SessionController: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Client authentication failed: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("Exception in SessionController: " + e.getMessage());
            // Log the full stack trace for unexpected errors for internal review
            e.printStackTrace(); 
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", e);
        }
    }
}
