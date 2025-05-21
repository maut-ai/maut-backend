package com.maut.core.modules.user.model; // Adjust package if User model exists elsewhere

import java.util.UUID;
// Minimal User class for compilation
public class User {
    private UUID id;
    private String email;
    // Add other fields like team memberships if needed for permission checks

    public User(UUID id, String email) { this.id = id; this.email = email; }
    public UUID getId() { return id; }
    public String getEmail() { return email; }
    // Ensure this placeholder is sufficient for WebhookSubscriptionServiceImpl's needs
}
