package com.maut.core;

// import com.maut.core.common.config.ApplicationConfig; // No longer explicitly imported
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.context.annotation.Import; // No longer explicitly imported
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the Maut Core Backend.
 * This service implements a modular monolith architecture with multiple functional modules.
 * Each module follows the Controller-Service-Repository pattern for clean separation of concerns.
 */
@SpringBootApplication
@EnableScheduling // Enable scheduling for cron tasks across modules
// @Import(ApplicationConfig.class) // Removed: component scan should suffice
public class MautCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(MautCoreApplication.class, args);
    }
}
