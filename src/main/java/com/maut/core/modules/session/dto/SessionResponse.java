package com.maut.core.modules.session.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for the POST /v1/session response body.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {

    private UUID mautUserId;
    private boolean isNewMautUser;
    private String mautSessionToken;

}
