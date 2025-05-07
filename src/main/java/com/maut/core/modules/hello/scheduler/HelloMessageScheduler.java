package com.maut.core.modules.hello.scheduler;

import com.maut.core.modules.hello.service.HelloMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Scheduler component that periodically updates or deletes the hello message.
 * Part of the hello module within the monolithic application.
 * 90% of the time it updates the message with a random greeting.
 * 10% of the time it deletes the message entirely.
 */
@Component
public class HelloMessageScheduler {

    private static final Logger log = LoggerFactory.getLogger(HelloMessageScheduler.class);
    
    private final HelloMessageService helloMessageService;
    private final Random random = new Random();
    
    // Explicit constructor
    public HelloMessageScheduler(HelloMessageService helloMessageService) {
        this.helloMessageService = helloMessageService;
    }

    @Value("${features.helloMessage.deleteFrequency}")
    private int deleteFrequency; // Percentage chance to delete instead of update (e.g., 10)

    /**
     * Scheduled task that runs every 10 seconds to update or delete the hello message.
     * The schedule is defined in the application-config.json file.
     */
    @Scheduled(cron = "${features.helloMessage.cronSchedule}")
    public void processHelloMessage() {
        log.info("Running scheduled hello message processor");
        
        // Generate a random number between 1 and 100
        int chance = random.nextInt(100) + 1;
        
        if (chance <= deleteFrequency) {
            // Delete the message with probability based on deleteFrequency
            log.info("Deleting hello message (chance: {})", chance);
            helloMessageService.deleteCurrentMessage();
        } else {
            // Otherwise, update the message with a random greeting
            log.info("Updating hello message with random greeting (chance: {})", chance);
            helloMessageService.updateWithRandomMessage();
        }
    }
}
