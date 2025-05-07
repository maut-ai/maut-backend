package com.maut.core.common.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Configuration properties class for database settings.
 * Maps the database section from application-config.json to Java properties.
 */
@Data
@Component
@ConfigurationProperties(prefix = "database")
public class DatabaseProperties {
    
    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private JpaProperties jpa;
    private FlywayProperties flyway;
    
    /**
     * JPA specific configuration properties.
     */
    @Data
    public static class JpaProperties {
        private String hibernateDdlAuto;
        private boolean showSql;
        private Map<String, Object> properties;
    }
    
    /**
     * Flyway specific configuration properties.
     */
    @Data
    public static class FlywayProperties {
        private boolean enabled;
        private boolean baselineOnMigrate;
        private List<String> locations;
    }
}
