package com.maut.core.modules.auth.controller;

import com.maut.core.modules.auth.dto.ClientRegistrationRequest;
import com.maut.core.modules.auth.dto.LoginRequest;
import com.maut.core.modules.auth.dto.LoginResponse;
import com.maut.core.modules.auth.dto.CurrentUserResponseDto;
import com.maut.core.modules.auth.service.AuthService;
import com.maut.core.modules.auth.service.JwtService;
import com.maut.core.modules.role.model.AdminRole;
import com.maut.core.modules.team.dto.TeamSummaryDto;
import com.maut.core.modules.user.model.User;
import com.maut.core.modules.user.enums.UserType;
import com.maut.core.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserDetailsService userDetailsService;

    @PostMapping("/client/register")
    public ResponseEntity<?> registerClient(@Valid @RequestBody ClientRegistrationRequest request) {
        log.info("Received client registration request for email: {}", request.getEmail());
        try {
            User registeredUser = authService.registerClient(request);

            UserDetails userDetails = userDetailsService.loadUserByUsername(registeredUser.getEmail());

            String jwt = jwtService.generateToken(userDetails);
            log.info("Client registration successful, token generated for email: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(new LoginResponse(jwt));
        } catch (Exception e) {
            log.error("Client registration failed for email: {}: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/admin/login")
    public ResponseEntity<?> loginAdmin(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Admin login attempt for email: {}", loginRequest.getEmail());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("User not found after authentication"));

            if (user.getUserType() != UserType.ADMIN) {
                log.warn("Admin login attempt by non-ADMIN user: {}", loginRequest.getEmail());
                throw new BadCredentialsException("User is not authorized for admin login.");
            }

            String jwt = jwtService.generateToken(userDetails);
            log.info("Admin login successful, token generated for email: {}", loginRequest.getEmail());
            return ResponseEntity.ok(new LoginResponse(jwt));
        } catch (BadCredentialsException e) {
            log.warn("Admin login failed for email {}: {}", loginRequest.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error during admin login for email {}: {}", loginRequest.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Login failed due to an internal error.");
        }
    }

    @PostMapping("/client/login")
    public ResponseEntity<?> loginClient(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Client login attempt for email: {}", loginRequest.getEmail());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("User not found after authentication"));

            if (user.getUserType() != UserType.CLIENT) {
                log.warn("Client login attempt by non-CLIENT user: {}", loginRequest.getEmail());
                throw new BadCredentialsException("User is not authorized for client login.");
            }

            String jwt = jwtService.generateToken(userDetails);
            log.info("Client login successful, token generated for email: {}", loginRequest.getEmail());
            return ResponseEntity.ok(new LoginResponse(jwt));
        } catch (BadCredentialsException e) {
            log.warn("Client login failed for email {}: {}", loginRequest.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error during client login for email {}: {}", loginRequest.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Login failed due to an internal error.");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            log.warn("Attempt to access /me endpoint without authentication.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }

        log.info("Fetching current user details for: {}", userDetails.getUsername());
        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("User not found in repository despite being authenticated."));

            TeamSummaryDto teamSummaryDto = null;
            if (user.getTeam() != null) {
                teamSummaryDto = new TeamSummaryDto(user.getTeam().getId(), user.getTeam().getName());
            }

            Set<String> roleNames = user.getAdminRoles().stream()
                    .map(AdminRole::getName)
                    .collect(Collectors.toSet());

            CurrentUserResponseDto responseDto = CurrentUserResponseDto.builder()
                    .id(user.getId())
                    .userType(user.getUserType())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .isActive(user.isActive())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .team(teamSummaryDto)
                    .roles(roleNames)
                    .build();

            log.info("Successfully fetched current user details for: {}", userDetails.getUsername());
            return ResponseEntity.ok(responseDto);
        } catch (BadCredentialsException e) {
            log.warn("Could not find authenticated user {} in repository: {}", userDetails.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error fetching current user details for {}: {}", userDetails.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve user details.");
        }
    }
}
