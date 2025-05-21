# Webhook System Core Requirements

## 1. Introduction

This document outlines the core requirements for the new webhook system. This system will enable external client applications to receive real-time notifications about various events occurring within our platform, facilitating better integration and responsiveness.

## 2. Key Event Types

Events are categorized to provide clarity and allow for granular subscription. The following are key event types that the webhook system should support.

### 2.1. Activity Events

Events related to the lifecycle and status changes of various activities or processes within the system.

*   **`activity.created`**
    *   **Description:** Triggered when a new activity or process is initiated.
    *   **Example:** A new data export job is started.
*   **`activity.status.updated`**
    *   **Description:** Fired when the status of an ongoing activity changes.
    *   **Example:** An export job changes from `processing` to `completed` or `failed`.
*   **`activity.completed`**
    *   **Description:** A more specific event indicating an activity has successfully finished.
    *   **Example:** An export job successfully completes.
*   **`activity.failed`**
    *   **Description:** A more specific event indicating an activity has failed.
    *   **Example:** An export job fails due to an error.

### 2.2. Wallet Events (Transaction Focused)

Events specifically related to financial transactions and wallet activities.

*   **`transaction.initiated`**
    *   **Description:** Triggered when a new transaction (e.g., payment, withdrawal, transfer) is initiated by a user or the system. The transaction is typically in a pending state.
*   **`transaction.succeeded`**
    *   **Description:** Fired when a transaction has been successfully processed and confirmed.
*   **`transaction.failed`**
    *   **Description:** Occurs when a transaction fails to complete (e.g., due to insufficient funds, risk rejection, or processing errors).
*   **`transaction.status.updated`**
    *   **Description:** Generic event for any status change in a transaction not covered by succeeded/failed, e.g., `pending_approval`, `requires_action`.
*   **`wallet.balance.updated`**
    *   **Description:** Triggered when a user's wallet balance changes due to a completed transaction or other adjustments. (Consider if this is too noisy; `transaction.succeeded` might be preferred by some clients).

### 2.3. User Events

Events related to user account management and lifecycle.

*   **`user.created`**
    *   **Description:** Triggered when a new user account is successfully registered.
*   **`user.updated`**
    *   **Description:** Fired when a user's profile information (e.g., name, email, contact details) is updated.
*   **`user.deleted`**
    *   **Description:** Occurs when a user account is deleted from the system.
*   **`user.login.succeeded`**
    *   **Description:** Triggered upon a successful user login.
*   **`user.login.failed`**
    *   **Description:** Fired when a login attempt fails.

### 2.4. Client Application Events

Events related to the management and status of external client applications.

*   **`client.application.created`**
    *   **Description:** Triggered when a new external client application is registered.
*   **`client.application.updated`**
    *   **Description:** Fired when the configuration or settings of a client application are updated (e.g., redirect URIs, name).
*   **`client.application.deleted`**
    *   **Description:** Occurs when a client application is de-registered or deleted.
*   **`client.application.credentials.revoked`**
    *   **Description:** Triggered if a client application's credentials are revoked.

### 2.5. Security Events

Events related to account security and potential threats.

*   **`security.alert.triggered`**
    *   **Description:** Fired when a security event or potential threat is detected (e.g., suspicious login attempt, unusual API activity).
    *   **Note:** The payload for this event should be carefully designed to provide actionable information without exposing sensitive details.
*   **`security.password.changed`**
    *   **Description:** Triggered when a user successfully changes their account password.
*   **`security.2fa.status.updated`**
    *   **Description:** Occurs when a user enables or disables two-factor authentication.

### 2.6. Policy Events

Events related to changes in platform policies or terms.

*   **`policy.updated`**
    *   **Description:** Triggered when there are significant updates to API usage policies, terms of service, or other guiding documents.
    *   **Example:** `terms_of_service.updated`, `api_usage_policy.updated`.

## 3. Standard Event Payload Structure

All webhook events will share a common JSON structure to ensure consistency and ease of processing by client applications.

```json
{
  "eventId": "evt_xxxxxxxxxxxxxxx",
  "eventType": "category.event.action",
  "eventVersion": "1.0",
  "timestamp": "YYYY-MM-DDTHH:mm:ss.sssZ",
  "userId": "usr_xxxxxxxxxxxxxxx", // Optional: Present if the event is directly related to a specific user
  "clientApplicationId": "app_xxxxxxxxxxxxxxx", // Optional: Present if the event is scoped to a client app or triggered by one
  "data": {
    // Event-specific details will be nested here
  }
}
```

**Field Descriptions:**

*   **`eventId`** (String): A unique identifier for this specific event instance (e.g., UUID).
*   **`eventType`** (String): The specific type of event that occurred, using dot notation (e.g., `transaction.succeeded`).
*   **`eventVersion`** (String): The version of the event payload schema for this `eventType`. Allows for versioning of event structures over time.
*   **`timestamp`** (String): ISO 8601 UTC timestamp indicating when the event occurred.
*   **`userId`** (String, Optional): The unique identifier of the user associated with this event, if applicable.
*   **`clientApplicationId`** (String, Optional): The unique identifier of the client application associated with this event, if applicable (e.g., if the event was triggered by an action of a specific API client).
*   **`data`** (Object): An object containing event-specific information. The structure of this object varies depending on the `eventType`.

### 3.1. Example `data` Object for `activity.status.updated`

```json
{
  "eventId": "evt_abc123xyz789",
  "eventType": "activity.status.updated",
  "eventVersion": "1.0",
  "timestamp": "2023-11-15T10:00:00.000Z",
  "userId": "usr_user123",
  "data": {
    "activityId": "act_export456",
    "activityType": "data_export",
    "previousStatus": "processing",
    "currentStatus": "completed",
    "details": {
      "message": "Data export completed successfully.",
      "exportedFileUrl": "https://example.com/exports/export456.zip" // Example detail
    }
  }
}
```

### 3.2. Example `data` Object for `transaction.succeeded`

```json
{
  "eventId": "evt_def456uvw012",
  "eventType": "transaction.succeeded",
  "eventVersion": "1.0",
  "timestamp": "2023-11-15T10:05:00.000Z",
  "userId": "usr_user789",
  "data": {
    "transactionId": "txn_ghi789jkl012",
    "walletId": "wal_user789main",
    "type": "payment", // e.g., payment, withdrawal, deposit, transfer
    "amount": "100.50",
    "currency": "USD",
    "description": "Payment for Order #ORD12345",
    "relatedParty": { // Optional, details of the other party if applicable
      "type": "merchant",
      "identifier": "merch_xyz"
    }
  }
}
```

## 4. Subscription Model

The subscription model defines how client applications register to receive webhook notifications.

### 4.1. Subscriber Entity

*   Subscriptions are managed at the `ClientApplication` level. Each registered client application can have multiple webhook subscriptions, though typically one active subscription per application is common.

### 4.2. Subscription Configuration

To create or update a webhook subscription, the following information will be required:

*   **`targetUrl`** (String): The HTTPS URL on the client application's server where webhook POST requests will be sent. This URL must be publicly accessible.
*   **`eventTypes`** (Array of Strings): A list of specific `eventType`s the client application wishes to subscribe to (e.g., `["transaction.succeeded", "user.updated"]`).
*   **`secret`** (String): A cryptographically strong secret key provided by the client application during subscription setup. This secret is used by the client application to verify the authenticity of incoming webhooks via signature checking (e.g., HMAC-SHA256). Our system will use this secret to sign each webhook request. This secret should be configurable and updatable by the client application owner.

### 4.3. Subscription Scope and Behavior

*   **Explicit Event Selection:** Client applications **must** explicitly specify the `eventTypes` they want to receive. There will be no "subscribe to all events" option by default. This prevents overwhelming clients with unnecessary data and allows for better resource management on both sides.
*   **Multiple Subscriptions (Optional):** While a single `targetUrl` per application is standard, the system might allow multiple named subscriptions with different `targetUrl`s and/or `eventTypes` for advanced use cases, if deemed necessary. For V1, one active subscription endpoint per client application is recommended.
*   **Subscription Management:** Client applications should be able to create, view, update (e.g., change URL, event types, regenerate secret), and delete their webhook subscriptions through a developer portal or a dedicated API.

## 5. Security Considerations (Summary)

*   **HTTPS:** All `targetUrl`s must be HTTPS.
*   **Signature Verification:** Webhooks must be signed by our system using the shared `secret`, and client applications must verify these signatures. The specific hashing algorithm (e.g., HMAC-SHA256) will be documented.
*   **Secret Management:** Secure storage and handling of the `secret` by both the client and our system is paramount.
*   **Replay Attack Prevention:** Including a `timestamp` in the signed payload and potentially a nonce (`eventId` can serve this purpose if unique and unpredictable) can help mitigate replay attacks. Clients should check the `timestamp` to ensure freshness.
*   **Idempotency:** Clients should design their webhook handlers to be idempotent, as network issues might lead to retries and duplicate event delivery. `eventId` can be used for deduplication.

This document provides the foundational requirements. Further detailed specifications will be needed for API design, retry policies, error handling, and monitoring.
