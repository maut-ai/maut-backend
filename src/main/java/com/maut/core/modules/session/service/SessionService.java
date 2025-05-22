package com.maut.core.modules.session.service;

import com.maut.core.modules.session.dto.SessionRequest;
import com.maut.core.modules.session.dto.SessionResponse;
import com.maut.core.modules.session.security.JwtUtil;
import com.maut.core.modules.clientapplication.model.ClientApplication;
import com.maut.core.modules.user.model.MautUser;
import com.maut.core.modules.clientapplication.repository.ClientApplicationRepository;
import com.maut.core.modules.user.repository.MautUserRepository;
import com.maut.core.modules.webhook.service.WebhookDispatcherService;
import com.maut.core.common.events.WebhookEventTypes;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);

    private final JwtUtil jwtUtil;
    private final ClientApplicationRepository clientApplicationRepository;
    private final MautUserRepository mautUserRepository;
    private final WebhookDispatcherService webhookDispatcherService;

    @Transactional
    public SessionResponse processClientSession(SessionRequest sessionRequest) {
        String clientAuthToken = sessionRequest.getClientAuthToken();

        String mautApiClientId = jwtUtil.extractIssuerFromUnverifiedClientToken(clientAuthToken);
        if (mautApiClientId == null) {
            throw new SecurityException("Invalid clientAuthToken: Unable to extract issuer.");
        }

        ClientApplication clientApp = clientApplicationRepository.findByMautApiClientId(mautApiClientId)
                .orElseThrow(() -> new SecurityException("Invalid clientAuthToken: Client application not found for issuer: " + mautApiClientId));

        if (!jwtUtil.validateClientAuthToken(clientAuthToken, clientApp)) {
            throw new SecurityException("Invalid clientAuthToken: Token validation failed.");
        }

        String clientSystemUserId = jwtUtil.extractClientSystemUserIdFromClientToken(clientAuthToken, clientApp);
        if (clientSystemUserId == null) {
            throw new SecurityException("Invalid clientAuthToken: Unable to extract clientSystemUserId.");
        }

        boolean isNewMautUser;
        MautUser mautUser;
        
        Optional<MautUser> existingUser = mautUserRepository.findByClientApplicationAndClientSystemUserId(clientApp, clientSystemUserId);
        if (existingUser.isPresent()) {
            mautUser = existingUser.get();
            isNewMautUser = false;
        } else {
            MautUser newUser = new MautUser();
            newUser.setMautUserId(UUID.randomUUID());
            newUser.setClientApplication(clientApp);
            newUser.setClientSystemUserId(clientSystemUserId);
            if (clientApp.getTeam() != null) { 
                newUser.setTeam(clientApp.getTeam());
            }
            mautUser = mautUserRepository.save(newUser);
            isNewMautUser = true;

            // Dispatch webhook event for MautUser creation
            if (mautUser.getTeam() != null) {
                try {
                    log.info("Dispatching '{}' event for MautUser ID: {} and team ID: {}", 
                             WebhookEventTypes.MAUT_USER_CREATED, mautUser.getMautUserId(), mautUser.getTeam().getId());
                    webhookDispatcherService.dispatchEvent(
                        mautUser.getTeam().getId(), 
                        WebhookEventTypes.MAUT_USER_CREATED, 
                        mautUser
                    );
                } catch (Exception e) {
                    log.error("Failed to dispatch '{}' event for MautUser ID: {} and team ID: {}. Error: {}", 
                              WebhookEventTypes.MAUT_USER_CREATED, mautUser.getMautUserId(), mautUser.getTeam().getId(), e.getMessage(), e);
                    // Not re-throwing, to not let webhook failure break session processing
                }
            } else {
                log.warn("New MautUser ID: {} was created but has no associated team. Webhook event '{}' not dispatched.", 
                         mautUser.getMautUserId(), WebhookEventTypes.MAUT_USER_CREATED);
            }
        }

        String mautSessionToken = jwtUtil.generateMautSessionToken(mautUser);

        return new SessionResponse(mautUser.getMautUserId(), isNewMautUser, mautSessionToken);
    }

    public MautUser validateMautSessionTokenAndGetMautUser(String mautSessionToken) {
        if (mautSessionToken == null || mautSessionToken.isBlank()) {
            throw new SecurityException("Maut session token is missing or empty.");
        }

        String mautUserIdString;
        Date expirationDate;

        try {
            mautUserIdString = jwtUtil.extractMautUserIdFromMautSession(mautSessionToken);
            expirationDate = jwtUtil.extractExpirationFromMautSession(mautSessionToken);
        } catch (io.jsonwebtoken.JwtException e) {
            // This catches parsing errors, signature errors, etc.
            throw new SecurityException("Invalid Maut session token: " + e.getMessage(), e);
        }

        if (mautUserIdString == null) {
            throw new SecurityException("Invalid Maut session token: MautUser ID not found in token.");
        }

        if (expirationDate == null || expirationDate.before(new Date())) {
            throw new SecurityException("Invalid Maut session token: Token is expired.");
        }

        UUID mautUserId;
        try {
            mautUserId = UUID.fromString(mautUserIdString);
        } catch (IllegalArgumentException e) {
            throw new SecurityException("Invalid Maut session token: Malformed MautUser ID.", e);
        }

        return mautUserRepository.findByMautUserId(mautUserId)
                .orElseThrow(() -> new SecurityException("Invalid Maut session token: MautUser not found."));
    }
}
