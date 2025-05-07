package com.maut.core.common.config;

import com.maut.core.common.config.properties.DatabaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * Database configuration class that sets up the datasource, entity manager,
 * and transaction manager using properties from application-config.json.
 */
@Configuration
@EnableTransactionManagement
@EnableConfigurationProperties(DatabaseProperties.class)
public class DatabaseConfig {

    private final DatabaseProperties databaseProperties;

    public DatabaseConfig(DatabaseProperties databaseProperties) {
        this.databaseProperties = databaseProperties;
    }

    /**
     * Configures the datasource using properties from the JSON config.
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        
        // Get property values with fallbacks
        String url = getPropertyWithFallback(databaseProperties.getUrl(), "jdbc:postgresql://localhost:5432/maut_core");
        String username = getPropertyWithFallback(databaseProperties.getUsername(), "postgres");
        String password = getPropertyWithFallback(databaseProperties.getPassword(), "postgres");
        String driverClassName = getPropertyWithFallback(databaseProperties.getDriverClassName(), "org.postgresql.Driver");
        
        // Set properties with fallback values
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);
        
        return dataSource;
    }
    
    /**
     * Helper method to provide fallback values for database properties
     * @param value The value from configuration
     * @param fallback The fallback value to use if the configuration value is null or empty
     * @return The configuration value or fallback if empty
     */
    private String getPropertyWithFallback(String value, String fallback) {
        return (value == null || value.trim().isEmpty()) ? fallback : value;
    }

    /**
     * Configures the entity manager factory with JPA and Hibernate properties.
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.maut.core");
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        
        // Safely get JPA properties with null checks
        boolean showSql = databaseProperties.getJpa() != null && databaseProperties.getJpa().isShowSql();
        vendorAdapter.setShowSql(showSql);
        em.setJpaVendorAdapter(vendorAdapter);
        
        Properties jpaProperties = new Properties();
        
        // Add hibernate.hbm2ddl.auto with a fallback
        String hibernateDdlAuto = databaseProperties.getJpa() != null && databaseProperties.getJpa().getHibernateDdlAuto() != null ? 
                                 databaseProperties.getJpa().getHibernateDdlAuto() : "validate";
        jpaProperties.put("hibernate.hbm2ddl.auto", hibernateDdlAuto);
        
        // Add dialect with default
        jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        
        // Add any additional Hibernate properties from the configuration
        if (databaseProperties.getJpa() != null && databaseProperties.getJpa().getProperties() != null) {
            databaseProperties.getJpa().getProperties().forEach(jpaProperties::put);
        }
        
        em.setJpaProperties(jpaProperties);
        return em;
    }

    /**
     * Configures the transaction manager.
     */
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }

    // Removed custom Flyway migration bean to let Spring Boot's auto-configuration handle it
}
