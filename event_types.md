# Event Types for External Developers

This document outlines potential event types that external developers can subscribe to, enabling them to receive real-time notifications for various activities and changes within the system. These events are crucial for building responsive and integrated applications.

## Event Categories and Types

Events are categorized based on the services and activities they relate to.

### 1. Wallet Activity Events

These events notify developers about activities related to user wallets.

*   **`wallet.transaction.created`**
    *   **Description:** Triggered when a new transaction is initiated from or to a user's wallet (e.g., sending or attempting to receive funds). The status might be pending.
*   **`wallet.transaction.completed`**
    *   **Description:** Fired when a transaction (either incoming or outgoing) has been successfully processed and confirmed.
*   **`wallet.transaction.failed`**
    *   **Description:** Occurs when a transaction fails to complete due to errors, insufficient funds, or other issues.
*   **`wallet.funds.received`**
    *   **Description:** Specifically indicates that funds have been successfully credited to a user's wallet.
*   **`wallet.funds.sent`**
    *   **Description:** Specifically indicates that funds have been successfully debited from a user's wallet.

### 2. User Account Events

Events related to user account lifecycle and profile modifications.

*   **`user.account.created`**
    *   **Description:** Triggered when a new user account is successfully registered.
*   **`user.login.successful`**
    *   **Description:** Fired upon a successful user login event.
*   **`user.login.failed`**
    *   **Description:** Occurs when a login attempt fails (e.g., due to incorrect credentials).
*   **`user.profile.updated`**
    *   **Description:** Triggered when a user updates their profile information (e.g., name, email, contact details).
*   **`user.settings.updated`**
    *   **Description:** Fired when a user changes their account settings (e.g., notification preferences, security settings).

### 3. Application and Policy Events

Events related to client applications, policy updates, and system-wide announcements.

*   **`application.client.registered`**
    *   **Description:** Triggered when a new external client application is registered to use the API.
*   **`policy.updated`**
    *   **Description:** Fired when there are changes or updates to API usage policies, terms of service, or other relevant legal/operational documents.
*   **`system.announcement`**
    *   **Description:** Used for broadcasting important system-wide announcements, such as maintenance schedules or new feature rollouts.

### 4. Security Events

Events related to account security and potential threats.

*   **`security.alert.suspicious_activity`**
    *   **Description:** Triggered when suspicious activity is detected on a user's account (e.g., multiple failed login attempts, login from an unrecognized device).
*   **`security.password.changed`**
    *   **Description:** Fired when a user successfully changes their account password.
*   **`security.2fa.status.updated`**
    *   **Description:** Occurs when a user enables or disables two-factor authentication.

### 5. Activity Status Change Events

Generic events that track the progression of various activities or processes within the system.

*   **`activity.status.pending_approval`**
    *   **Description:** Fired when an activity (e.g., a high-value transaction, a request for certain permissions) is initiated and is awaiting manual or automated approval.
*   **`activity.status.approved`**
    *   **Description:** Triggered when a pending activity has been approved.
*   **`activity.status.rejected`**
    *   **Description:** Occurs when a pending activity has been rejected.
*   **`activity.status.completed`**
    *   **Description:** Generic event indicating an operation or process has successfully completed (useful for asynchronous operations not covered by more specific events).
*   **`activity.status.failed`**
    *   **Description:** Generic event indicating an operation or process has failed (useful for asynchronous operations not covered by more specific events).

## Subscribing to Events (Webhook System)

External developers can subscribe to these events using a webhook-based system. This allows your application to receive real-time HTTP notifications when specific events occur.

### How to Subscribe:

1.  **Register a Webhook URL:**
    *   Developers need to register a publicly accessible HTTPS URL endpoint in their developer portal or via a dedicated API endpoint (e.g., `/webhooks/register`).
    *   This URL will receive POST requests containing event data in JSON format.
2.  **Select Event Types:**
    *   During webhook registration or modification, developers can specify which event types they are interested in (e.g., `wallet.transaction.completed`, `user.profile.updated`). Subscribing only to necessary events reduces unnecessary traffic.
3.  **Secure Your Webhook:**
    *   It is crucial to secure your webhook endpoint. This typically involves:
        *   **Signature Verification:** Webhooks from our system will be signed (e.g., using HMAC-SHA256) with a secret key provided during registration. Your endpoint should verify this signature to ensure the request originated from our system.
        *   **HTTPS:** Your endpoint must use HTTPS.
4.  **Acknowledge Events:**
    *   Your webhook endpoint should respond with a `2xx` HTTP status code (e.g., `200 OK` or `202 Accepted`) promptly to acknowledge receipt of an event. If our system does not receive a timely acknowledgment, it may retry sending the event.

### Event Payload Example:

When an event occurs, an HTTP POST request will be sent to the registered webhook URL with a JSON payload similar to this:

```json
{
  "event_id": "evt_1234567890abcdef",
  "event_type": "wallet.transaction.completed",
  "timestamp": "2023-10-27T10:30:00Z",
  "data": {
    "transaction_id": "txn_abcdef123456",
    "wallet_id": "wal_user123wallet",
    "amount": "1.2345",
    "currency": "BTC",
    "status": "completed",
    "description": "Payment for order #XYZ123"
  },
  "user_id": "usr_user123"
}
```

**Note:** The exact structure of the `data` object will vary depending on the `event_type`. Detailed schemas for each event payload would be available in the main API documentation.

If specific details about the webhook system (e.g., the exact registration endpoint, signature verification methods) are not found in `docs/api-documentation.md` or `docs/api_definitions.md`, this section on "Subscribing to Events" should be considered a general guideline, and clarification should be sought from the API provider. For the purpose of this document, its existence is assumed based on common best practices.
