package com.maut.core.modules.clientapplication.service;
import com.maut.core.modules.user.model.User;
import com.maut.core.modules.webhook.exception.PermissionDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service("webhookClientApplicationService") // Qualify if multiple impls exist
public class DummyClientApplicationServiceImpl implements ClientApplicationService {
    private static final Logger log = LoggerFactory.getLogger(DummyClientApplicationServiceImpl.class);

    @Override
    public void verifyUserAccessToClientApplication(User user, UUID clientApplicationId) throws PermissionDeniedException {
        // TODO: Implement actual permission logic (e.g., check team membership, user roles, ownership)
        // For this subtask, we perform a basic non-null check as a placeholder.
        if (user == null) {
            log.warn("Permission denied: Authenticated user is null for client application {}", clientApplicationId);
            throw new PermissionDeniedException("User authentication is required.");
        }
        if (clientApplicationId == null) {
            log.warn("Permission denied: ClientApplicationID is null for user {}", user.getId());
            throw new PermissionDeniedException("ClientApplicationID cannot be null.");
        }
        // Placeholder: Log access attempt. Replace with actual permission verification.
        log.info("Placeholder: Verifying access for user {} ({}) to client application {}",
                user.getId(), user.getEmail(), clientApplicationId);
        // Example of a denied access for specific user/client for testing (remove in real app):
        // if (user.getId().toString().equals("some-user-id-to-block") && clientApplicationId.toString().equals("some-client-app-id-to-block")) {
        //     log.warn("Access explicitly denied for testing: User {} to Client App {}", user.getId(), clientApplicationId);
        //     throw new PermissionDeniedException("Access denied for user " + user.getId() + " to client application " + clientApplicationId);
        // }
    }
}
