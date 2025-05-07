package com.maut.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the Maut Core Backend.
 * This service implements a modular monolith architecture with multiple functional modules.
 * Each module follows the Controller-Service-Repository pattern for clean separation of concerns.
 */
@SpringBootApplication
@EnableScheduling // Enable scheduling for cron tasks across modules
public class MautCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(MautCoreApplication.class, args);
    }
}
