package com.maut.core.modules.webhook.service;

import java.util.UUID;

public interface WebhookDispatcherService {

    /**
     * Dispatches a webhook event to all subscribed and active webhooks for a given team and event type.
     *
     * @param teamId The ID of the team that owns the resource triggering the event.
     * @param eventType The type of event being dispatched (e.g., WebhookEventTypes.USER_ACCOUNT_CREATED).
     * @param payload The data payload for the event, which will be serialized to JSON.
     */
    void dispatchEvent(UUID teamId, String eventType, Object payload);

}
