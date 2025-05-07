package com.maut.core.modules.hello.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maut.core.modules.hello.dto.HelloMessageDto;
import com.maut.core.modules.hello.model.HelloMessage;
import com.maut.core.modules.hello.service.HelloMessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for the HelloController.
 * Tests the controller in isolation using WebMvcTest.
 */
@WebMvcTest(HelloController.class)
class HelloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HelloMessageService helloMessageService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getHelloMessage_shouldReturnMessage_whenMessageExists() throws Exception {
        // Arrange
        HelloMessage mockMessage = new HelloMessage();
        mockMessage.setId(1L);
        mockMessage.setMessage("Hello Test");
        mockMessage.setUpdatedAt(LocalDateTime.now());
        
        when(helloMessageService.getCurrentMessage()).thenReturn(mockMessage);

        // Act & Assert
        mockMvc.perform(get("/v1/hello")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.message", is("Hello Test")));
    }

    @Test
    void getHelloMessage_shouldReturnNotFound_whenNoMessageExists() throws Exception {
        // Arrange
        when(helloMessageService.getCurrentMessage()).thenThrow(new EntityNotFoundException("No hello message found"));

        // Act & Assert
        mockMvc.perform(get("/v1/hello")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void createHelloMessage_shouldCreateAndReturnMessage() throws Exception {
        // Arrange
        HelloMessageDto requestDto = new HelloMessageDto();
        requestDto.setMessage("New Hello Message");

        HelloMessage createdMessage = new HelloMessage();
        createdMessage.setId(2L);
        createdMessage.setMessage("New Hello Message");
        createdMessage.setUpdatedAt(LocalDateTime.now());
        
        when(helloMessageService.createMessage(anyString())).thenReturn(createdMessage);

        // Act & Assert
        mockMvc.perform(post("/v1/hello")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.message", is("New Hello Message")));
    }

    @Test
    void createHelloMessage_shouldReturnBadRequest_whenMessageIsInvalid() throws Exception {
        // Arrange
        HelloMessageDto requestDto = new HelloMessageDto();
        requestDto.setMessage(""); // Empty message should fail validation

        // Act & Assert
        mockMvc.perform(post("/v1/hello")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }
}
