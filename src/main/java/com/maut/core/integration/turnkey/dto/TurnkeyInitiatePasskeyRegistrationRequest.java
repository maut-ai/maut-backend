package com.maut.core.integration.turnkey.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TurnkeyInitiatePasskeyRegistrationRequest {
    // The Maut user ID, to associate the Turnkey authenticator with our user
    private String mautUserId;
    // The Turnkey sub-organization ID under which this user/passkey will be created
    private String turnkeySubOrganizationId;
    // A user-friendly name for the authenticator (e.g., "Sam's MacBook Pro")
    private String authenticatorName;
    // Potentially other parameters like relying party ID if not globally configured
}
