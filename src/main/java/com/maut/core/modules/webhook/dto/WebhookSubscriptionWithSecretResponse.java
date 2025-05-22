package com.maut.core.modules.webhook.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
public class WebhookSubscriptionWithSecretResponse extends WebhookSubscriptionResponse {
    private String secret;

    @Builder(builderMethodName = "withSecretBuilder")
    public WebhookSubscriptionWithSecretResponse(UUID id, String targetUrl, List<String> eventTypes, boolean isActive, OffsetDateTime createdAt, OffsetDateTime updatedAt, String secret) {
        super(id, targetUrl, eventTypes, isActive, createdAt, updatedAt);
        this.secret = secret;
    }
}
