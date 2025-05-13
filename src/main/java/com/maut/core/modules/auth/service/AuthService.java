package com.maut.core.modules.auth.service;

import com.maut.core.modules.auth.dto.ClientRegistrationRequest;
import com.maut.core.modules.user.model.User;

public interface AuthService {

    /**
     * Registers a new client user, creates their initial team, and sets them as the owner.
     *
     * @param request The registration request details.
     * @return The newly created User entity.
     * @throws Exception // Define more specific exceptions later (e.g., EmailExistsException, PasswordMismatchException)
     */
    User registerClient(ClientRegistrationRequest request) throws Exception;
}
