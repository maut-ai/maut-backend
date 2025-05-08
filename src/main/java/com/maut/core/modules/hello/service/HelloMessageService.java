package com.maut.core.modules.hello.service;

import com.maut.core.modules.hello.model.HelloMessage;
import com.maut.core.modules.hello.repository.HelloMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Service class handling business logic related to HelloMessage entities.
 * Part of the hello module within the monolithic application.
 * Follows the single responsibility principle by focusing only on hello message operations.
 */
@Service
public class HelloMessageService {

    private static final Logger log = LoggerFactory.getLogger(HelloMessageService.class);

    private final HelloMessageRepository helloMessageRepository;
    private final Random random = new Random();
    
    // Explicit constructor
    public HelloMessageService(HelloMessageRepository helloMessageRepository) {
        this.helloMessageRepository = helloMessageRepository;
    }
    
    // List of random greetings to choose from when updating the message
    private static final List<String> RANDOM_GREETINGS = Arrays.asList(
            "Hello, World!",
            "Greetings, Human!",
            "Hi there!",
            "Welcome to our service!",
            "Good day to you!",
            "Howdy, partner!",
            "Hey, how's it going?",
            "Salutations!",
            "Bonjour!",
            "Hola!",
            "What's kicking, little chicken?",
            "Aloha!",
            "Ciao!",
            "Shalom!",
            "Namaste!",
            "Konnichiwa!",
            "Guten Tag!",
            "Zdravstvuyte!",
            "Ahoy, matey!",
            "Top o' the mornin' to ya!",
            "What's shakin', bacon?",
            "How hangs it?",
            "Good to see you!",
            "Long time no see!",
            "A pleasure to meet you.",
            "May the Force be with you.",
            "Greetings, Earthling!",
            "How do you do?",
            "Nice to make your acquaintance.",
            "What's new?",
            "Hope you're having a great day!",
            "Look what the cat dragged in!",
            "Hey, good lookin'!",
            "What's cookin'?",
            "How's life treating you?",
            "Peek-a-boo!",
            "Yo!",
            "Alright, guv'nor?",
            "Sup?",
            "Greetings and salutations!",
            "Well, hello there!",
            "How are things?",
            "Fancy meeting you here!",
            "Good morrow!",
            "Stay awhile and listen!",
            "Live long and prosper.",
            "Peace and long life.",
            "What it do?",
            "Greetings, program!",
            "Wotcher!"
    );

    /**
     * Retrieves the current hello message.
     * @return the most recent hello message
     * @throws EntityNotFoundException if no message exists
     */
    @Transactional(readOnly = true)
    public HelloMessage getCurrentMessage() {
        HelloMessage message = helloMessageRepository.findTopByOrderByUpdatedAtDesc();
        if (message == null) {
            log.error("No hello message found in the database");
            throw new EntityNotFoundException("No hello message found");
        }
        return message;
    }

    /**
     * Creates a new hello message with the provided content.
     * @param message the message content
     * @return the created HelloMessage entity
     */
    @Transactional
    public HelloMessage createMessage(String message) {
        log.info("Creating new hello message: {}", message);
        HelloMessage helloMessage = new HelloMessage();
        helloMessage.setMessage(message);
        return helloMessageRepository.save(helloMessage);
    }

    /**
     * Updates the hello message with a randomly selected greeting.
     * @return the updated HelloMessage entity
     */
    @Transactional
    public HelloMessage updateWithRandomMessage() {
        String randomGreeting = RANDOM_GREETINGS.get(random.nextInt(RANDOM_GREETINGS.size()));
        log.info("Updating hello message with random greeting: {}", randomGreeting);
        
        HelloMessage existingMessage = helloMessageRepository.findTopByOrderByUpdatedAtDesc();
        if (existingMessage != null) {
            existingMessage.setMessage(randomGreeting);
            return helloMessageRepository.save(existingMessage);
        } else {
            return createMessage(randomGreeting);
        }
    }

    /**
     * Deletes the current hello message if one exists.
     */
    @Transactional
    public void deleteCurrentMessage() {
        HelloMessage existingMessage = helloMessageRepository.findTopByOrderByUpdatedAtDesc();
        if (existingMessage != null) {
            log.info("Deleting hello message with ID: {}", existingMessage.getId());
            helloMessageRepository.delete(existingMessage);
        } else {
            log.warn("No hello message to delete");
        }
    }
}
