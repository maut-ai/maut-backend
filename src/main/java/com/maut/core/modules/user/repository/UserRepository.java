package com.maut.core.modules.user.repository;

import com.maut.core.modules.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds a user by their email address.
     *
     * @param email The email address to search for.
     * @return An Optional containing the user if found, or empty otherwise.
     */
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
