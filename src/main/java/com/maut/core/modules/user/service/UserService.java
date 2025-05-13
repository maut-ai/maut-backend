package com.maut.core.modules.user.service;

import com.maut.core.modules.user.model.User;
import com.maut.core.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor 
public class UserService {

    private final UserRepository userRepository;

    /**
     * Finds a user by their email address.
     *
     * @param email The email address to search for.
     * @return An Optional containing the user if found, or empty otherwise.
     */
    @Transactional(readOnly = true) 
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Saves a new user or updates an existing one.
     *
     * @param user The user entity to save.
     * @return The saved user entity (possibly with generated ID).
     */
    @Transactional 
    public User createUser(User user) {
        // Add any pre-save logic here if needed (e.g., validation, setting defaults)
        return userRepository.save(user);
    }
}
