package com.maut.core.modules.webhook.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maut.core.modules.webhook.model.WebhookSubscription;
import com.maut.core.modules.webhook.repository.WebhookSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookDispatcherServiceImpl implements WebhookDispatcherService {

    private final WebhookSubscriptionRepository webhookSubscriptionRepository;
    private final ObjectMapper objectMapper; // For JSON serialization
    private final RestTemplate restTemplate; // For sending HTTP requests

    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

    @Override
    public void dispatchEvent(UUID teamId, String eventType, Object payload) {
        log.info("Dispatching event type '{}' for team ID '{}'", eventType, teamId);
        List<WebhookSubscription> subscriptions = webhookSubscriptionRepository
                .findActiveByTeamIdAndMatchingEventType(teamId, eventType);

        if (subscriptions.isEmpty()) {
            log.info("No active subscriptions found for event type '{}' and team ID '{}'", eventType, teamId);
            return;
        }

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize payload for event type '{}', team ID '{}'. Error: {}", eventType, teamId, e.getMessage());
            return; // Cannot proceed without a valid JSON payload
        }

        for (WebhookSubscription subscription : subscriptions) {
            log.debug("Processing subscription ID '{}' for target URL '{}'", subscription.getId(), subscription.getTargetUrl());
            try {
                String signature = generateSignature(payloadJson, subscription.getSecret());

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("X-Maut-Signature-SHA256", signature); // Standard signature header
                headers.set("X-Maut-Event-Type", eventType);

                HttpEntity<String> entity = new HttpEntity<>(payloadJson, headers);

                // Consider making this asynchronous in a future iteration
                restTemplate.exchange(subscription.getTargetUrl(), HttpMethod.POST, entity, String.class);
                log.info("Successfully dispatched event to subscription ID '{}', target URL '{}'", subscription.getId(), subscription.getTargetUrl());

            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                log.error("Security error (HMAC generation) for subscription ID '{}': {}. Event not sent.", subscription.getId(), e.getMessage());
                // Depending on policy, might mark subscription as problematic
            } catch (Exception e) {
                log.error("Failed to dispatch event to subscription ID '{}', target URL '{}'. Error: {}", 
                          subscription.getId(), subscription.getTargetUrl(), e.getMessage());
                // Implement retry logic or dead-letter queue in future if needed
            }
        }
    }

    private String generateSignature(String payload, String secret) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256Hmac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256_ALGORITHM);
        sha256Hmac.init(secretKey);
        byte[] signedBytes = sha256Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signedBytes);
    }
}
