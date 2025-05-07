package com.maut.core.modules.hello.controller;

import com.maut.core.modules.hello.dto.HelloMessageDto;
import com.maut.core.modules.hello.model.HelloMessage;
import com.maut.core.modules.hello.service.HelloMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Controller handling hello message endpoints.
 * Part of the hello module within the monolithic application.
 * Following API versioning standard with v1 in the path.
 */
@RestController
@RequestMapping("/v1/hello")
@RequiredArgsConstructor
@Slf4j
public class HelloController {

    private final HelloMessageService helloMessageService;

    /**
     * Get the current hello message.
     * @return ResponseEntity containing the HelloMessageDto
     */
    @GetMapping
    public ResponseEntity<HelloMessageDto> getHelloMessage() {
        log.debug("REST request to get current hello message");
        HelloMessage message = helloMessageService.getCurrentMessage();
        return ResponseEntity.ok(mapToDto(message));
    }

    /**
     * Create a new hello message.
     * @param dto DTO containing the new message content
     * @return ResponseEntity containing the created HelloMessageDto
     */
    @PostMapping
    public ResponseEntity<HelloMessageDto> createHelloMessage(@Valid @RequestBody HelloMessageDto dto) {
        log.debug("REST request to create a new hello message: {}", dto);
        HelloMessage created = helloMessageService.createMessage(dto.getMessage());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToDto(created));
    }

    /**
     * Maps the HelloMessage entity to a HelloMessageDto.
     * @param entity The HelloMessage entity
     * @return The corresponding HelloMessageDto
     */
    private HelloMessageDto mapToDto(HelloMessage entity) {
        return HelloMessageDto.builder()
                .id(entity.getId())
                .message(entity.getMessage())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
