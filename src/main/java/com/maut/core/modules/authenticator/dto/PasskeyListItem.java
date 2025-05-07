package com.maut.core.modules.authenticator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasskeyListItem {

    private String id; // Corresponds to UserAuthenticator id (UUID)
    private String name; // User-friendly name for the passkey, e.g., "YubiKey 5C" or "MacBook Pro Touch ID"
    private String credentialId; // The WebAuthn credential ID (base64url encoded)
    private Instant createdAt;
    private Instant lastUsedAt; // Optional: when the passkey was last used
    private String type; // e.g., "PLATFORM", "CROSS_PLATFORM" (from UserAuthenticatorType)
    private boolean enabled; // Whether the passkey is currently active

}
