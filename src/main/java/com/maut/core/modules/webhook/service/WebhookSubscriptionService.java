package com.maut.core.modules.webhook.service;

import com.maut.core.modules.user.model.User; // Adjust if User location is different
import com.maut.core.modules.webhook.dto.*;
import java.util.List;
import java.util.UUID;

public interface WebhookSubscriptionService {
    WebhookSubscriptionWithSecretResponse createWebhookSubscription(UUID clientApplicationId, CreateWebhookSubscriptionRequest request, User authenticatedUser);
    WebhookSubscriptionResponse getWebhookSubscription(UUID clientApplicationId, UUID webhookId, User authenticatedUser);
    List<WebhookSubscriptionResponse> listWebhookSubscriptions(UUID clientApplicationId, User authenticatedUser);
    WebhookSubscriptionResponse updateWebhookSubscription(UUID clientApplicationId, UUID webhookId, UpdateWebhookSubscriptionRequest request, User authenticatedUser);
    void deleteWebhookSubscription(UUID clientApplicationId, UUID webhookId, User authenticatedUser);
}
