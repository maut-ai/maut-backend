package com.maut.core.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.PostConstruct;

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
    private static final Logger log = LoggerFactory.getLogger(ApplicationConfig.class);

    private WebAuthnConfig webauthn;

    public WebAuthnConfig getWebauthn() {
        return webauthn;
    }

    public void setWebauthn(WebAuthnConfig webauthn) {
        log.info("ApplicationConfig.setWebauthn() called.");
        if (webauthn == null) {
            log.warn("ApplicationConfig.setWebauthn() called with null WebAuthnConfig.");
        } else {
            log.info("WebAuthnConfig received in setter: RP ID = '{}', RP Name = '{}', Origins = {}",
                    webauthn.getRelyingPartyId(),
                    webauthn.getRelyingPartyName(),
                    webauthn.getRelyingPartyOrigins());
            if (webauthn.getRelyingPartyId() == null) {
                log.warn("WebAuthnConfig received in setter has null relyingPartyId.");
            }
            if (webauthn.getRelyingPartyName() == null) {
                log.warn("WebAuthnConfig received in setter has null relyingPartyName.");
            }
        }
        this.webauthn = webauthn;
    }

    @PostConstruct
    public void init() {
        log.info("ApplicationConfig @PostConstruct validation:");
        if (this.webauthn == null) {
            log.warn("After initialization (PostConstruct), this.webauthn is NULL.");
        } else {
            log.info("After initialization (PostConstruct), this.webauthn.getRelyingPartyId() = '{}'", this.webauthn.getRelyingPartyId());
            log.info("After initialization (PostConstruct), this.webauthn.getRelyingPartyName() = '{}'", this.webauthn.getRelyingPartyName());
            if (this.webauthn.getRelyingPartyId() == null || this.webauthn.getRelyingPartyName() == null) {
                log.error("CRITICAL: WebAuthn config is incomplete after initialization in ApplicationConfig (PostConstruct)!");
            } else {
                log.info("WebAuthn config appears to be correctly loaded in ApplicationConfig (PostConstruct).");
            }
        }
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
