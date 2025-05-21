package com.maut.core.modules.webhook.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class WebhookSubscriptionResponse {
    private UUID id;
    private String targetUrl;
    private List<String> eventTypes;
    private boolean isActive;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
