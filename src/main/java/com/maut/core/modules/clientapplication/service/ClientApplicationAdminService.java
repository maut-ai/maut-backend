package com.maut.core.modules.clientapplication.service;

import com.maut.core.modules.clientapplication.dto.CreateClientApplicationRequest;
import com.maut.core.modules.clientapplication.dto.CreateClientApplicationResponse;
import com.maut.core.modules.clientapplication.model.ClientApplication;
import com.maut.core.modules.clientapplication.repository.ClientApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientApplicationAdminService {

    private final ClientApplicationRepository clientApplicationRepository;
    private static final int SECRET_LENGTH = 32; // Length of the random part of the secret in bytes

    @Transactional
    public CreateClientApplicationResponse createClientApplication(CreateClientApplicationRequest request) {
        log.info("Attempting to create a new client application with name: {}", request.getClientName());

        if (clientApplicationRepository.findByClientName(request.getClientName()).isPresent()) {
            log.warn("Client application with name '{}' already exists.", request.getClientName());
            // Consider throwing a specific exception like DuplicateResourceException
            throw new IllegalArgumentException("Client application with name '" + request.getClientName() + "' already exists.");
        }

        String mautApiClientId = "cid-" + UUID.randomUUID().toString();
        String plainTextSecret = generateSecureRandomSecret();

        ClientApplication newClientApp = new ClientApplication();
        newClientApp.setClientName(request.getClientName());
        newClientApp.setMautApiClientId(mautApiClientId);
        newClientApp.setClientSecret(plainTextSecret);
        newClientApp.setEnabled(true); // Enabled by default
        // newClientApp.setAllowedOrigins(new HashSet<>()); // Initialize if needed, or handle via update endpoint

        ClientApplication savedClientApp = clientApplicationRepository.save(newClientApp);
        log.info("Successfully created client application '{}' with ID: {}", savedClientApp.getClientName(), savedClientApp.getMautApiClientId());

        return new CreateClientApplicationResponse(
                savedClientApp.getMautApiClientId(),
                savedClientApp.getClientName(),
                plainTextSecret // Return the plaintext secret only upon creation
        );
    }

    private String generateSecureRandomSecret() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[SECRET_LENGTH];
        secureRandom.nextBytes(randomBytes);
        // Prefixing with "msk_" (Maut Secret Key) for identifiability, though it's not strictly part of the random secret itself.
        // The actual secret used for signing by the client would be this full string.
        return "msk_" + Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
