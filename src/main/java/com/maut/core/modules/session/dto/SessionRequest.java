package com.maut.core.modules.session.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for the POST /v1/session request body.
 */
@Data
@NoArgsConstructor
public class SessionRequest {

    @NotBlank(message = "clientAuthToken is mandatory")
    private String clientAuthToken;
    
}
