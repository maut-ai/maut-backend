package com.maut.core.common.config.properties;

import com.maut.core.common.config.JsonPropertySourceFactory;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Configuration properties class for database settings.
 * Maps the database section from application-config.json to Java properties.
 */
@Data
@Component
@PropertySource(value = "classpath:config/application-config.json", factory = JsonPropertySourceFactory.class)
@ConfigurationProperties(prefix = "database")
public class DatabaseProperties {
    
    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private JpaProperties jpa;
    private FlywayProperties flyway;
    
    public String getUrl() {
        return url;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public String getDriverClassName() {
        return driverClassName;
    }
    
    public JpaProperties getJpa() {
        return jpa;
    }
    
    public FlywayProperties getFlyway() {
        return flyway;
    }
    
    /**
     * JPA specific configuration properties.
     */
    @Data
    public static class JpaProperties {
        private String hibernateDdlAuto;
        private boolean showSql;
        private Map<String, Object> properties;
        
        public String getHibernateDdlAuto() {
            return hibernateDdlAuto;
        }
        
        public boolean isShowSql() {
            return showSql;
        }
        
        public Map<String, Object> getProperties() {
            return properties;
        }
    }
    
    /**
     * Flyway specific configuration properties.
     */
    @Data
    public static class FlywayProperties {
        private boolean enabled;
        private boolean baselineOnMigrate;
        private boolean cleanDisabled;
        private boolean validateOnMigrate;
        private boolean outOfOrder;
        private List<String> locations;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public boolean isBaselineOnMigrate() {
            return baselineOnMigrate;
        }
        
        public boolean isCleanDisabled() {
            return cleanDisabled;
        }
        
        public boolean isValidateOnMigrate() {
            return validateOnMigrate;
        }
        
        public boolean isOutOfOrder() {
            return outOfOrder;
        }
        
        public List<String> getLocations() {
            return locations;
        }
    }
}
