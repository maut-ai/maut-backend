package com.maut.core.modules.authenticator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitiatePasskeyRegistrationResponse {

    private String turnkeyChallenge;
    private Map<String, Object> turnkeyAttestationRequest; // Or a more specific DTO if structure is known

}
