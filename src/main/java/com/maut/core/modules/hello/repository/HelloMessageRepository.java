package com.maut.core.modules.hello.repository;

import com.maut.core.modules.hello.model.HelloMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for HelloMessage entities.
 * Extends JpaRepository to inherit standard CRUD operations.
 * Part of the hello module within the monolithic application.
 */
@Repository
public interface HelloMessageRepository extends JpaRepository<HelloMessage, Long> {
    
    /**
     * Custom query method to find the most recently updated hello message.
     * @return the most recent HelloMessage or null if none exists
     */
    HelloMessage findTopByOrderByUpdatedAtDesc();
}
