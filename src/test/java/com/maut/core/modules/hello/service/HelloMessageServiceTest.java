package com.maut.core.modules.hello.service;

import com.maut.core.modules.hello.model.HelloMessage;
import com.maut.core.modules.hello.repository.HelloMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HelloMessageServiceTest {

    @Mock
    private HelloMessageRepository helloMessageRepository;

    @InjectMocks
    private HelloMessageService helloMessageService;

    private HelloMessage testMessage;

    @BeforeEach
    void setUp() {
        // Set up test data
        testMessage = new HelloMessage();
        testMessage.setId(1L);
        testMessage.setMessage("Test Hello Message");
        testMessage.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void getCurrentMessage_whenMessageExists_returnsMessage() {
        // Arrange
        when(helloMessageRepository.findTopByOrderByUpdatedAtDesc()).thenReturn(testMessage);

        // Act
        HelloMessage result = helloMessageService.getCurrentMessage();

        // Assert
        assertNotNull(result);
        assertEquals(testMessage.getId(), result.getId());
        assertEquals(testMessage.getMessage(), result.getMessage());
        verify(helloMessageRepository, times(1)).findTopByOrderByUpdatedAtDesc();
    }

    @Test
    void getCurrentMessage_whenNoMessageExists_throwsException() {
        // Arrange
        when(helloMessageRepository.findTopByOrderByUpdatedAtDesc()).thenReturn(null);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            helloMessageService.getCurrentMessage();
        });
        verify(helloMessageRepository, times(1)).findTopByOrderByUpdatedAtDesc();
    }

    @Test
    void createMessage_savesNewMessage() {
        // Arrange
        String newMessage = "New Hello Message";
        when(helloMessageRepository.save(any(HelloMessage.class))).thenAnswer(invocation -> {
            HelloMessage savedMessage = invocation.getArgument(0);
            savedMessage.setId(2L);
            savedMessage.setUpdatedAt(LocalDateTime.now());
            return savedMessage;
        });

        // Act
        HelloMessage result = helloMessageService.createMessage(newMessage);

        // Assert
        assertNotNull(result);
        assertEquals(newMessage, result.getMessage());
        assertNotNull(result.getId());
        assertNotNull(result.getUpdatedAt());
        verify(helloMessageRepository, times(1)).save(any(HelloMessage.class));
    }

    @Test
    void updateWithRandomMessage_whenMessageExists_updatesExistingMessage() {
        // Arrange
        when(helloMessageRepository.findTopByOrderByUpdatedAtDesc()).thenReturn(testMessage);
        when(helloMessageRepository.save(any(HelloMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        HelloMessage result = helloMessageService.updateWithRandomMessage();

        // Assert
        assertNotNull(result);
        // Verify the message was changed (to one of the random greetings)
        assertNotEquals("", result.getMessage());
        assertEquals(testMessage.getId(), result.getId());
        verify(helloMessageRepository, times(1)).findTopByOrderByUpdatedAtDesc();
        verify(helloMessageRepository, times(1)).save(any(HelloMessage.class));
    }

    @Test
    void updateWithRandomMessage_whenNoMessageExists_createsNewMessage() {
        // Arrange
        when(helloMessageRepository.findTopByOrderByUpdatedAtDesc()).thenReturn(null);
        when(helloMessageRepository.save(any(HelloMessage.class))).thenAnswer(invocation -> {
            HelloMessage savedMessage = invocation.getArgument(0);
            savedMessage.setId(3L);
            savedMessage.setUpdatedAt(LocalDateTime.now());
            return savedMessage;
        });

        // Act
        HelloMessage result = helloMessageService.updateWithRandomMessage();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getMessage());
        assertNotNull(result.getId());
        assertNotNull(result.getUpdatedAt());
        verify(helloMessageRepository, times(1)).findTopByOrderByUpdatedAtDesc();
        verify(helloMessageRepository, times(1)).save(any(HelloMessage.class));
    }

    @Test
    void deleteCurrentMessage_whenMessageExists_deletesMessage() {
        // Arrange
        when(helloMessageRepository.findTopByOrderByUpdatedAtDesc()).thenReturn(testMessage);
        doNothing().when(helloMessageRepository).delete(any(HelloMessage.class));

        // Act
        helloMessageService.deleteCurrentMessage();

        // Assert
        verify(helloMessageRepository, times(1)).findTopByOrderByUpdatedAtDesc();
        verify(helloMessageRepository, times(1)).delete(testMessage);
    }

    @Test
    void deleteCurrentMessage_whenNoMessageExists_doesNothing() {
        // Arrange
        when(helloMessageRepository.findTopByOrderByUpdatedAtDesc()).thenReturn(null);

        // Act
        helloMessageService.deleteCurrentMessage();

        // Assert
        verify(helloMessageRepository, times(1)).findTopByOrderByUpdatedAtDesc();
        verify(helloMessageRepository, never()).delete(any(HelloMessage.class));
    }
}
