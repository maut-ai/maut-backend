package com.maut.core.modules.clientapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyClientApplicationResponse {
    // For now, let's assume it returns a list of application names or some identifiers
    // This can be expanded once a ClientApplication entity is defined.
    private List<String> applicationNames;
    private String message;

    // Example static constructor for a simple message
    public static MyClientApplicationResponse success(String message) {
        return MyClientApplicationResponse.builder()
                .applicationNames(Collections.emptyList()) // Placeholder
                .message(message)
                .build();
    }
}
