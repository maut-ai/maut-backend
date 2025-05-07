package com.maut.core.modules.session.service;

import com.maut.core.modules.session.dto.SessionRequest; // Updated import
import com.maut.core.modules.session.dto.SessionResponse; // Updated import
import com.maut.core.modules.session.security.JwtUtil; // Updated import
import com.maut.core.modules.clientapplication.model.ClientApplication; // Updated import
import com.maut.core.modules.user.model.MautUser; // Updated import
import com.maut.core.modules.clientapplication.repository.ClientApplicationRepository; // Updated import
import com.maut.core.modules.user.repository.MautUserRepository; // Updated import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class SessionService {

    private final JwtUtil jwtUtil;
    private final ClientApplicationRepository clientApplicationRepository;
    private final MautUserRepository mautUserRepository;

    @Autowired
    public SessionService(JwtUtil jwtUtil,
                          ClientApplicationRepository clientApplicationRepository,
                          MautUserRepository mautUserRepository) {
        this.jwtUtil = jwtUtil;
        this.clientApplicationRepository = clientApplicationRepository;
        this.mautUserRepository = mautUserRepository;
    }

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
            mautUser = mautUserRepository.save(newUser);
            isNewMautUser = true;
        }

        String mautSessionToken = jwtUtil.generateMautSessionToken(mautUser);

        return new SessionResponse(mautUser.getMautUserId(), isNewMautUser, mautSessionToken);
    }
}
