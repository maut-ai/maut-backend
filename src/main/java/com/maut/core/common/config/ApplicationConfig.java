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
public class ApplicationConfig {
    // Configuration properties can be injected into other components using @Value or ConfigurationProperties
}
