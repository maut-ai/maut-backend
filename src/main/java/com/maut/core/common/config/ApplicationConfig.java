package com.maut.core.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Application-wide configuration class that loads settings from the application-config.json file.
 * Uses the custom JsonPropertySourceFactory to parse JSON configurations.
 * 
 * This configuration applies across all modules and contains global settings.
 * Module-specific configurations extend or utilize this base configuration.
 */
@Configuration
@PropertySource(
    value = "classpath:config/application-config.json", 
    factory = JsonPropertySourceFactory.class
)
// @PropertySource("classpath:config/test-features.properties") // Load standard properties file
public class ApplicationConfig {
    // Configuration properties can be injected into other components using @Value or ConfigurationProperties

    private WebAuthnConfig webauthn;

    public WebAuthnConfig getWebauthn() {
        return webauthn;
    }

    public void setWebauthn(WebAuthnConfig webauthn) { // Setter needed for Spring's data binding
        this.webauthn = webauthn;
    }

    // Static inner class to hold WebAuthn specific configurations
    public static class WebAuthnConfig {
        private String relyingPartyId;
        private String relyingPartyName;
        private java.util.List<String> relyingPartyOrigins;
        private Long registrationTimeoutMs;
        private Long authenticationTimeoutMs;

        // Getters and Setters for all fields
        public String getRelyingPartyId() {
            return relyingPartyId;
        }

        public void setRelyingPartyId(String relyingPartyId) {
            this.relyingPartyId = relyingPartyId;
        }

        public String getRelyingPartyName() {
            return relyingPartyName;
        }

        public void setRelyingPartyName(String relyingPartyName) {
            this.relyingPartyName = relyingPartyName;
        }

        public java.util.List<String> getRelyingPartyOrigins() {
            return relyingPartyOrigins;
        }

        public void setRelyingPartyOrigins(java.util.List<String> relyingPartyOrigins) {
            this.relyingPartyOrigins = relyingPartyOrigins;
        }

        public Long getRegistrationTimeoutMs() {
            return registrationTimeoutMs;
        }

        public void setRegistrationTimeoutMs(Long registrationTimeoutMs) {
            this.registrationTimeoutMs = registrationTimeoutMs;
        }

        public Long getAuthenticationTimeoutMs() {
            return authenticationTimeoutMs;
        }

        public void setAuthenticationTimeoutMs(Long authenticationTimeoutMs) {
            this.authenticationTimeoutMs = authenticationTimeoutMs;
        }
    }
}
