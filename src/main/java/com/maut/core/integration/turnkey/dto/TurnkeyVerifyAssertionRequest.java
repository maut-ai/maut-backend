package com.maut.core.integration.turnkey.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TurnkeyVerifyAssertionRequest {
    // The Turnkey sub-organization ID where the authenticator resides
    private String turnkeySubOrganizationId;
    // The ID of the passkey credential to be verified
    private String passkeyCredentialId; // This is the UserAuthenticator.externalAuthenticatorId
    // The assertion object (PublicKeyCredential) from the client, as a JSON string or structured object
    private String assertion;
    // The clientDataJSON from the authenticator response, as a Base64URL-encoded string
    private String clientDataJSON;
    // The authenticatorData from the authenticator response, as a Base64URL-encoded string
    private String authenticatorData;
    // The signature from the authenticator response, as a Base64URL-encoded string
    private String signature;
    // The original challenge that was sent to the client
    private String originalChallenge;
}
