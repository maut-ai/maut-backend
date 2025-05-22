package com.maut.core.modules.webhook.service;

import com.maut.core.modules.team.model.Team;
import com.maut.core.modules.team.repository.TeamRepository;
import com.maut.core.modules.user.model.User;
import com.maut.core.modules.webhook.dto.*;
import com.maut.core.modules.webhook.exception.ConflictException;
import com.maut.core.modules.webhook.exception.PermissionDeniedException;
import com.maut.core.modules.webhook.exception.ResourceNotFoundException;
import com.maut.core.modules.webhook.model.WebhookSubscription;
import com.maut.core.modules.webhook.repository.WebhookSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final TeamRepository teamRepository;

    private Team getTeamFromUser(User authenticatedUser) {
        return teamRepository.findByOwner(authenticatedUser)
                .orElseThrow(() -> {
                    log.warn("User {} is not an owner of any team. Webhook operation denied.", authenticatedUser.getId());
                    return new PermissionDeniedException("User must be an owner of a team to manage webhooks.");
                });
    }

    private String generateSecureSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32]; // 256 bits
        random.nextBytes(bytes);
        return "mws_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes); // "mws_" for "Maut Webhook Secret"
    }

    @Override
    public WebhookSubscriptionWithSecretResponse createWebhookSubscription(CreateWebhookSubscriptionRequest request, User authenticatedUser) {
        Team team = getTeamFromUser(authenticatedUser);
        log.info("User {} attempting to create webhook for team {}", authenticatedUser.getId(), team.getId());

        if (webhookSubscriptionRepository.existsByTeamIdAndTargetUrlAndActiveTrue(team.getId(), request.getTargetUrl())) {
            log.warn("Attempt to create webhook with duplicate active target URL {} for team {}", request.getTargetUrl(), team.getId());
            throw new ConflictException("An active webhook with this target URL already exists for this team.");
        }

        WebhookSubscription subscription = new WebhookSubscription();
        subscription.setTeamId(team.getId());
        subscription.setTargetUrl(request.getTargetUrl());
        subscription.setEventTypes(request.getEventTypes());
        subscription.setSecret(generateSecureSecret());
        subscription.setActive(true); // Default to active

        WebhookSubscription savedSubscription = webhookSubscriptionRepository.save(subscription);
        log.info("Webhook subscription created with ID {} for team {}", savedSubscription.getId(), team.getId());

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
    public WebhookSubscriptionResponse getWebhookSubscription(UUID webhookId, User authenticatedUser) {
        Team team = getTeamFromUser(authenticatedUser);
        log.info("User {} attempting to get webhook {} for team {}", authenticatedUser.getId(), webhookId, team.getId());
        WebhookSubscription subscription = webhookSubscriptionRepository.findByIdAndTeamId(webhookId, team.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Webhook subscription not found with ID: " + webhookId + " for this team."));
        return mapToResponse(subscription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WebhookSubscriptionResponse> listWebhookSubscriptions(User authenticatedUser) {
        Team team = getTeamFromUser(authenticatedUser);
        log.info("User {} listing webhooks for team {}", authenticatedUser.getId(), team.getId());
        List<WebhookSubscription> subscriptions = webhookSubscriptionRepository.findByTeamId(team.getId());
        return subscriptions.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public WebhookSubscriptionResponse updateWebhookSubscription(UUID webhookId, UpdateWebhookSubscriptionRequest request, User authenticatedUser) {
        Team team = getTeamFromUser(authenticatedUser);
        log.info("User {} attempting to update webhook {} for team {}", authenticatedUser.getId(), webhookId, team.getId());

        WebhookSubscription subscription = webhookSubscriptionRepository.findByIdAndTeamId(webhookId, team.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Webhook subscription not found with ID: " + webhookId + " for this team."));

        boolean updated = false;
        if (request.getTargetUrl() != null && !request.getTargetUrl().equals(subscription.getTargetUrl())) {
            boolean isActiveOrBeingActivated = (request.getIsActive() != null && request.getIsActive()) || (request.getIsActive() == null && subscription.isActive());
            if (isActiveOrBeingActivated && webhookSubscriptionRepository.existsByTeamIdAndTargetUrlAndActiveTrue(team.getId(), request.getTargetUrl())) {
                // Check if the conflicting webhook is this same webhook or another one
                if (!subscription.getTargetUrl().equals(request.getTargetUrl()) || !subscription.isActive()) {
                     // If it's a different webhook, or this one is being activated with a conflicting URL
                    WebhookSubscription conflictingSubscription = webhookSubscriptionRepository.findByTeamIdAndTargetUrlAndActiveTrue(team.getId(), request.getTargetUrl());
                    if (conflictingSubscription != null && !conflictingSubscription.getId().equals(webhookId)) {
                         log.warn("Attempt to update webhook {} to a duplicate active target URL {} for team {}. Conflicts with webhook {}.", webhookId, request.getTargetUrl(), team.getId(), conflictingSubscription.getId());
                         throw new ConflictException("An active webhook with the new target URL already exists for this team.");
                    }
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
            log.info("Webhook subscription {} updated for team {}", updatedSubscription.getId(), team.getId());
            return mapToResponse(updatedSubscription);
        } else {
             log.info("No changes detected for webhook subscription {} for team {}", webhookId, team.getId());
            return mapToResponse(subscription); // No changes, return current state
        }
    }

    @Override
    public void deleteWebhookSubscription(UUID webhookId, User authenticatedUser) {
        Team team = getTeamFromUser(authenticatedUser);
        log.info("User {} attempting to delete webhook {} for team {}", authenticatedUser.getId(), webhookId, team.getId());
        WebhookSubscription subscription = webhookSubscriptionRepository.findByIdAndTeamId(webhookId, team.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Webhook subscription not found with ID: " + webhookId + " for this team."));
        webhookSubscriptionRepository.delete(subscription);
        log.info("Webhook subscription {} deleted for team {}", webhookId, team.getId());
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
