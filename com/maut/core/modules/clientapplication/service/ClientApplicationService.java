package com.maut.core.modules.clientapplication.service; // Adjust package as needed

import com.maut.core.modules.user.model.User; // Adjust import for User
import com.maut.core.modules.webhook.exception.PermissionDeniedException; // Adjust import for exception
import java.util.UUID;

public interface ClientApplicationService {
    /**
     * Verifies if the authenticated user has access to the given client application.
     * Throws PermissionDeniedException if access is not allowed.
     */
    void verifyUserAccessToClientApplication(User user, UUID clientApplicationId) throws PermissionDeniedException;
}
