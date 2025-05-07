package com.maut.core.common.config;

import com.maut.core.common.config.properties.DatabaseProperties;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
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
@EnableConfigurationProperties
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
        dataSource.setUrl(databaseProperties.getUrl());
        dataSource.setUsername(databaseProperties.getUsername());
        dataSource.setPassword(databaseProperties.getPassword());
        dataSource.setDriverClassName(databaseProperties.getDriverClassName());
        return dataSource;
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
        vendorAdapter.setShowSql(databaseProperties.getJpa().isShowSql());
        em.setJpaVendorAdapter(vendorAdapter);
        
        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.hbm2ddl.auto", databaseProperties.getJpa().getHibernateDdlAuto());
        
        // Add any additional Hibernate properties from the configuration
        if (databaseProperties.getJpa().getProperties() != null) {
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

    /**
     * Configures Flyway for database migrations.
     */
    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            // Configure Flyway settings from the properties
            flyway.setBaselineOnMigrate(databaseProperties.getFlyway().isBaselineOnMigrate());
            
            // Set the locations if provided
            if (databaseProperties.getFlyway().getLocations() != null && !databaseProperties.getFlyway().getLocations().isEmpty()) {
                flyway.setLocations(databaseProperties.getFlyway().getLocations().toArray(new String[0]));
            }
            
            // Only run migrations if flyway is enabled
            if (databaseProperties.getFlyway().isEnabled()) {
                flyway.migrate();
            }
        };
    }
}
