package com.maut.core.modules.webhook.controller;

import com.maut.core.modules.user.model.User; // Adjust if User location is different
import com.maut.core.modules.webhook.dto.*;
import com.maut.core.modules.webhook.service.WebhookSubscriptionService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/webhooks") // Changed base path
@RequiredArgsConstructor
public class WebhookSubscriptionController {

    private final WebhookSubscriptionService webhookSubscriptionService;

    @PostMapping
    public ResponseEntity<WebhookSubscriptionWithSecretResponse> createWebhookSubscription(
            // @PathVariable UUID clientApplicationId, // Removed
            @Valid @RequestBody CreateWebhookSubscriptionRequest request,
            @AuthenticationPrincipal User authenticatedUser) {
        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        WebhookSubscriptionWithSecretResponse response = webhookSubscriptionService.createWebhookSubscription(request, authenticatedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{webhookId}")
    public ResponseEntity<WebhookSubscriptionResponse> getWebhookSubscription(
            // @PathVariable UUID clientApplicationId, // Removed
            @PathVariable UUID webhookId,
            @AuthenticationPrincipal User authenticatedUser) {
        if (authenticatedUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        WebhookSubscriptionResponse response = webhookSubscriptionService.getWebhookSubscription(webhookId, authenticatedUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<WebhookSubscriptionResponse>> listWebhookSubscriptions(
            // @PathVariable UUID clientApplicationId, // Removed
            @AuthenticationPrincipal User authenticatedUser) {
        if (authenticatedUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<WebhookSubscriptionResponse> responses = webhookSubscriptionService.listWebhookSubscriptions(authenticatedUser);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{webhookId}")
    public ResponseEntity<WebhookSubscriptionResponse> updateWebhookSubscription(
            // @PathVariable UUID clientApplicationId, // Removed
            @PathVariable UUID webhookId,
            @Valid @RequestBody UpdateWebhookSubscriptionRequest request,
            @AuthenticationPrincipal User authenticatedUser) {
        if (authenticatedUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        WebhookSubscriptionResponse response = webhookSubscriptionService.updateWebhookSubscription(webhookId, request, authenticatedUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{webhookId}")
    public ResponseEntity<Void> deleteWebhookSubscription(
            // @PathVariable UUID clientApplicationId, // Removed
            @PathVariable UUID webhookId,
            @AuthenticationPrincipal User authenticatedUser) {
        if (authenticatedUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        webhookSubscriptionService.deleteWebhookSubscription(webhookId, authenticatedUser);
        return ResponseEntity.noContent().build();
    }
}
