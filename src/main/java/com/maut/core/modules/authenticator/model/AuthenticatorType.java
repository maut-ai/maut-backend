package com.maut.core.modules.authenticator.model;

/**
 * Defines the types of authenticators a user can register.
 */
public enum AuthenticatorType {
    /**
     * A passkey authenticator (e.g., WebAuthn device, YubiKey, etc.).
     */
    PASSKEY,
    /**
     * Future authenticator types can be added here.
     * For example: OTP, EMAIL_MAGIC_LINK, etc.
     */
    // OTP, 
    // EMAIL_MAGIC_LINK 
}
