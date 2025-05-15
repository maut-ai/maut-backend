package com.maut.core.common.config;

import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.converter.util.ObjectConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for WebAuthn4J beans.
 */
@Configuration
public class WebAuthnConfiguration {

    /**
     * Provides a singleton bean of {@link ObjectConverter}.
     * This converter is used for serializing/deserializing WebAuthn-related objects
     * to/from JSON and CBOR formats.
     *
     * @return A configured {@link ObjectConverter} instance.
     */
    @Bean
    public ObjectConverter objectConverter() {
        // Creates an ObjectConverter with default internal ObjectMapper instances
        // for JSON and CBOR, pre-configured with necessary modules (e.g., JavaTimeModule).
        return new ObjectConverter();
    }

    /**
     * Provides a singleton bean of {@link WebAuthnManager}.
     * This manager is the central component for WebAuthn operations like registration
     * and authentication validation.
     *
     * @param objectConverter The {@link ObjectConverter} bean to be used by the WebAuthnManager.
     * @return A configured {@link WebAuthnManager} instance.
     */
    @Bean
    public WebAuthnManager webAuthnManager(ObjectConverter objectConverter) {
        // Creates a WebAuthnManager with default authenticator validators (for registration and authentication)
        // and the provided ObjectConverter. "NonStrict" typically implies more lenient validation rules,
        // which can be helpful for broader authenticator compatibility.
        // For stricter production environments, consider `WebAuthnManager.createDefaultWebAuthnManager()`
        // or manually providing a list of configured `AuthenticatorValidator`s.
        return WebAuthnManager.createNonStrictWebAuthnManager(objectConverter);
    }
}
