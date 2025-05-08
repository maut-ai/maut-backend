package com.maut.core.integration.turnkey.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TurnkeyFinalizePasskeyRegistrationRequest {
    // The Turnkey sub-organization ID where the registration was initiated
    private String turnkeySubOrganizationId;
    // The ID from the initiate phase (e.g., turnkeyAttestationId or a session ID)
    private String registrationContextId; // Or a more specific ID from the initiate phase
    // The attestation object (PublicKeyCredential) from the client, as a JSON string or structured object
    private String attestation;
    // The clientDataJSON from the authenticator response, as a Base64URL-encoded string
    private String clientDataJSON;
    // Transports (e.g. "usb", "nfc", "ble", "internal")
    private String[] transports;
}
