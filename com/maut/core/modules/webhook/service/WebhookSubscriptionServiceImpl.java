package com.maut.core.modules.webhook.service;

import com.maut.core.modules.clientapplication.service.ClientApplicationService; // Adjust import
import com.maut.core.modules.user.model.User; // Adjust import
import com.maut.core.modules.webhook.dto.*;
import com.maut.core.modules.webhook.exception.ConflictException;
import com.maut.core.modules.webhook.exception.PermissionDeniedException;
import com.maut.core.modules.webhook.exception.ResourceNotFoundException;
import com.maut.core.modules.webhook.model.WebhookSubscription;
import com.maut.core.modules.webhook.repository.WebhookSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WebhookSubscriptionServiceImpl implements WebhookSubscriptionService {

    private final WebhookSubscriptionRepository webhookSubscriptionRepository;
    // Using a placeholder/dummy ClientApplicationService for this task.
    // In a real app, inject the actual ClientApplicationService.
    @Qualifier("webhookClientApplicationService") // Or your actual qualifier/name
    private final ClientApplicationService clientApplicationService;


    private String generateSecureSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32]; // 256 bits
        random.nextBytes(bytes);
        return "mws_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes); // "mws_" for "Maut Webhook Secret"
    }

    @Override
    public WebhookSubscriptionWithSecretResponse createWebhookSubscription(UUID clientApplicationId, CreateWebhookSubscriptionRequest request, User authenticatedUser) {
        log.info("User {} attempting to create webhook for client application {}", authenticatedUser.getId(), clientApplicationId);
        clientApplicationService.verifyUserAccessToClientApplication(authenticatedUser, clientApplicationId);

        if (webhookSubscriptionRepository.existsByClientApplicationIdAndTargetUrlAndActiveTrue(clientApplicationId, request.getTargetUrl())) {
            log.warn("Attempt to create webhook with duplicate active target URL {} for client app {}", request.getTargetUrl(), clientApplicationId);
            throw new ConflictException("An active webhook with this target URL already exists for this client application.");
        }

        WebhookSubscription subscription = new WebhookSubscription();
        subscription.setClientApplicationId(clientApplicationId);
        subscription.setTargetUrl(request.getTargetUrl());
        subscription.setEventTypes(request.getEventTypes());
        subscription.setSecret(generateSecureSecret());
        subscription.setActive(true); // Default to active

        WebhookSubscription savedSubscription = webhookSubscriptionRepository.save(subscription);
        log.info("Webhook subscription created with ID {} for client application {}", savedSubscription.getId(), clientApplicationId);

        return WebhookSubscriptionWithSecretResponse.withSecretBuilder()
                .id(savedSubscription.getId())
                .targetUrl(savedSubscription.getTargetUrl())
                .eventTypes(savedSubscription.getEventTypes())
                .isActive(savedSubscription.isActive())
                .createdAt(savedSubscription.getCreatedAt())
                .updatedAt(savedSubscription.getUpdatedAt())
                .secret(savedSubscription.getSecret()) // Return secret only on create
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public WebhookSubscriptionResponse getWebhookSubscription(UUID clientApplicationId, UUID webhookId, User authenticatedUser) {
        clientApplicationService.verifyUserAccessToClientApplication(authenticatedUser, clientApplicationId);
        WebhookSubscription subscription = webhookSubscriptionRepository.findByIdAndClientApplicationId(webhookId, clientApplicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook subscription not found with ID: " + webhookId));
        return mapToResponse(subscription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WebhookSubscriptionResponse> listWebhookSubscriptions(UUID clientApplicationId, User authenticatedUser) {
        clientApplicationService.verifyUserAccessToClientApplication(authenticatedUser, clientApplicationId);
        List<WebhookSubscription> subscriptions = webhookSubscriptionRepository.findByClientApplicationId(clientApplicationId);
        return subscriptions.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public WebhookSubscriptionResponse updateWebhookSubscription(UUID clientApplicationId, UUID webhookId, UpdateWebhookSubscriptionRequest request, User authenticatedUser) {
        log.info("User {} attempting to update webhook {} for client application {}", authenticatedUser.getId(), webhookId, clientApplicationId);
        clientApplicationService.verifyUserAccessToClientApplication(authenticatedUser, clientApplicationId);
        WebhookSubscription subscription = webhookSubscriptionRepository.findByIdAndClientApplicationId(webhookId, clientApplicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook subscription not found with ID: " + webhookId));

        boolean updated = false;
        if (request.getTargetUrl() != null && !request.getTargetUrl().equals(subscription.getTargetUrl())) {
            // Check for duplicates if target URL is changing and the subscription is active or being activated
            boolean isActiveOrBeingActivated = (request.getIsActive() != null && request.getIsActive()) || (request.getIsActive() == null && subscription.isActive());
            if (isActiveOrBeingActivated && webhookSubscriptionRepository.existsByClientApplicationIdAndTargetUrlAndActiveTrue(clientApplicationId, request.getTargetUrl())) {
                 // Allow update to the same URL if it's the current subscription being modified (e.g. only changing event types)
                 // This check needs to ensure that we are not conflicting with *another* active subscription.
                if (!webhookSubscriptionRepository.findByIdAndClientApplicationId(webhookId, clientApplicationId)
                        .map(existingSub -> existingSub.getTargetUrl().equals(request.getTargetUrl()) && existingSub.isActive())
                        .orElse(false)) {
                    log.warn("Attempt to update webhook {} to a duplicate active target URL {} for client app {}", webhookId, request.getTargetUrl(), clientApplicationId);
                    throw new ConflictException("An active webhook with the new target URL already exists for this client application.");
                }
            }
            subscription.setTargetUrl(request.getTargetUrl());
            updated = true;
        }
        if (request.getEventTypes() != null && !request.getEventTypes().equals(subscription.getEventTypes())) {
            subscription.setEventTypes(request.getEventTypes());
            updated = true;
        }
        if (request.getIsActive() != null && request.getIsActive() != subscription.isActive()) {
            subscription.setActive(request.getIsActive());
            updated = true;
        }

        if (updated) {
            WebhookSubscription updatedSubscription = webhookSubscriptionRepository.save(subscription);
            log.info("Webhook subscription {} updated for client application {}", updatedSubscription.getId(), clientApplicationId);
            return mapToResponse(updatedSubscription);
        } else {
             log.info("No changes detected for webhook subscription {}", webhookId);
            return mapToResponse(subscription); // No changes, return current state
        }
    }

    @Override
    public void deleteWebhookSubscription(UUID clientApplicationId, UUID webhookId, User authenticatedUser) {
        log.info("User {} attempting to delete webhook {} for client application {}", authenticatedUser.getId(), webhookId, clientApplicationId);
        clientApplicationService.verifyUserAccessToClientApplication(authenticatedUser, clientApplicationId);
        WebhookSubscription subscription = webhookSubscriptionRepository.findByIdAndClientApplicationId(webhookId, clientApplicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook subscription not found with ID: " + webhookId + " for this client application."));
        webhookSubscriptionRepository.delete(subscription);
        log.info("Webhook subscription {} deleted for client application {}", webhookId, clientApplicationId);
    }

    private WebhookSubscriptionResponse mapToResponse(WebhookSubscription subscription) {
        return WebhookSubscriptionResponse.builder()
                .id(subscription.getId())
                .targetUrl(subscription.getTargetUrl())
                .eventTypes(subscription.getEventTypes())
                .isActive(subscription.isActive())
                .createdAt(subscription.getCreatedAt())
                .updatedAt(subscription.getUpdatedAt())
                .build();
    }
}
