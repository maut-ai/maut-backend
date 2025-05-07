package com.maut.core.modules.hello.scheduler;

import com.maut.core.modules.hello.service.HelloMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Random;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HelloMessageSchedulerTest {

    @Mock
    private HelloMessageService helloMessageService;

    @Mock
    private Random random;

    @Spy
    @InjectMocks
    private HelloMessageScheduler scheduler;

    @BeforeEach
    void setUp() {
        // Set the deleteFrequency via reflection to 10 (10% chance to delete)
        ReflectionTestUtils.setField(scheduler, "deleteFrequency", 10);
    }

    @Test
    void processHelloMessage_whenRandomIsLessThanOrEqualToDeleteFrequency_deletesMessage() {
        // Arrange
        when(random.nextInt(100)).thenReturn(9); // Returns 9, so chance will be 10 (below or equal to deleteFrequency)
        doNothing().when(helloMessageService).deleteCurrentMessage();

        // Act
        scheduler.processHelloMessage();

        // Assert
        verify(helloMessageService, times(1)).deleteCurrentMessage();
        verify(helloMessageService, never()).updateWithRandomMessage();
    }

    @Test
    void processHelloMessage_whenRandomIsGreaterThanDeleteFrequency_updatesMessage() {
        // Arrange
        when(random.nextInt(100)).thenReturn(10); // Returns 10, so chance will be 11 (above deleteFrequency)
        when(helloMessageService.updateWithRandomMessage()).thenReturn(null);

        // Act
        scheduler.processHelloMessage();

        // Assert
        verify(helloMessageService, never()).deleteCurrentMessage();
        verify(helloMessageService, times(1)).updateWithRandomMessage();
    }

    @Test
    void processHelloMessage_whenRandomIsEqualToDeleteFrequency_deletesMessage() {
        // Arrange - we want to test the exact boundary condition
        when(random.nextInt(100)).thenReturn(9); // Returns 9, so chance will be 10 (equal to deleteFrequency)
        doNothing().when(helloMessageService).deleteCurrentMessage();

        // Act
        scheduler.processHelloMessage();

        // Assert
        verify(helloMessageService, times(1)).deleteCurrentMessage();
        verify(helloMessageService, never()).updateWithRandomMessage();
    }
}
