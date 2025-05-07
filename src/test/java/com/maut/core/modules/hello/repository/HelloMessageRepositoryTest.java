package com.maut.core.modules.hello.repository;

import com.maut.core.modules.hello.model.HelloMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the HelloMessageRepository.
 * Uses an H2 in-memory database for testing JPA repositories.
 */
@DataJpaTest
@ActiveProfiles("test")
class HelloMessageRepositoryTest {

    @Autowired
    private HelloMessageRepository helloMessageRepository;

    @Test
    void findTopByOrderByUpdatedAtDesc_returnsLatestMessage() {
        // Arrange
        HelloMessage older = new HelloMessage();
        older.setMessage("Older message");
        older.setUpdatedAt(LocalDateTime.now().minus(1, ChronoUnit.HOURS));
        helloMessageRepository.save(older);

        HelloMessage newer = new HelloMessage();
        newer.setMessage("Newer message");
        newer.setUpdatedAt(LocalDateTime.now());
        helloMessageRepository.save(newer);

        // Act
        HelloMessage result = helloMessageRepository.findTopByOrderByUpdatedAtDesc();

        // Assert
        assertNotNull(result);
        assertEquals("Newer message", result.getMessage());
        assertEquals(newer.getId(), result.getId());
    }

    @Test
    void findTopByOrderByUpdatedAtDesc_whenNoMessages_returnsNull() {
        // Arrange - repository is empty by default in test

        // Act
        HelloMessage result = helloMessageRepository.findTopByOrderByUpdatedAtDesc();

        // Assert
        assertNull(result);
    }

    @Test
    void save_persists_newMessage() {
        // Arrange
        HelloMessage message = new HelloMessage();
        message.setMessage("Test message");
        message.setUpdatedAt(LocalDateTime.now());

        // Act
        HelloMessage savedMessage = helloMessageRepository.save(message);
        
        // Assert
        assertNotNull(savedMessage.getId());
        
        // Verify it's in the repository
        HelloMessage fetchedMessage = helloMessageRepository.findById(savedMessage.getId()).orElse(null);
        assertNotNull(fetchedMessage);
        assertEquals("Test message", fetchedMessage.getMessage());
    }
}
