package com.maut.core.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.maut.core.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public CustomAccessDeniedHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule()); // Ensure Java Time is handled
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) 
            throws IOException, ServletException {
        // Added diagnostic logging
        System.out.println("[CustomAccessDeniedHandler] Triggered for URI: " + request.getRequestURI());
        System.out.println("[CustomAccessDeniedHandler] Exception Message: " + accessDeniedException.getMessage());
        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            System.out.println("[CustomAccessDeniedHandler] Principal: " + authentication.getPrincipal());
            System.out.println("[CustomAccessDeniedHandler] Authorities: " + authentication.getAuthorities());
            System.out.println("[CustomAccessDeniedHandler] Is Authenticated: " + authentication.isAuthenticated());
        } else {
            System.out.println("[CustomAccessDeniedHandler] Authentication object is null.");
        }

        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                accessDeniedException.getMessage() != null ? accessDeniedException.getMessage() : "Access Denied", // Provide a default message
                request.getRequestURI()
        );

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
