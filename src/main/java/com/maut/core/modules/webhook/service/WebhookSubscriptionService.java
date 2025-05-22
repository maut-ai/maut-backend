package com.maut.core.modules.webhook.service;

import com.maut.core.modules.user.model.User; // Adjust if User location is different
import com.maut.core.modules.webhook.dto.*;
import java.util.List;
import java.util.UUID;

public interface WebhookSubscriptionService {
    WebhookSubscriptionWithSecretResponse createWebhookSubscription(CreateWebhookSubscriptionRequest request, User authenticatedUser);
    WebhookSubscriptionResponse getWebhookSubscription(UUID webhookId, User authenticatedUser);
    List<WebhookSubscriptionResponse> listWebhookSubscriptions(User authenticatedUser);
    WebhookSubscriptionResponse updateWebhookSubscription(UUID webhookId, UpdateWebhookSubscriptionRequest request, User authenticatedUser);
    void deleteWebhookSubscription(UUID webhookId, User authenticatedUser);
}
