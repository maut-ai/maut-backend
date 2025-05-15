# Vanilla WebAuthn Passkey Enrollment API Specification

This document outlines the specification for a "vanilla" WebAuthn passkey enrollment flow, bypassing Turnkey for these specific operations. This involves changes to the `/v1/authenticator/initiate-passkey-registration` and `/v1/authenticator/complete-passkey-registration` endpoints.

**Project Root:** `/Users/sam/maut/maut-backend`

## 1. Core Idea

*   **Initiation:** The client (frontend) requests the server to start passkey registration. The server generates `PublicKeyCredentialCreationOptions` (including a cryptographic challenge) and sends them to the client.
*   **Completion:** The client uses these options to interact with the browser's WebAuthn API (`navigator.credentials.create()`). The resulting new public key credential is sent back to the server. The server verifies this credential, including the challenge, and then stores it.

## 2. API Endpoints and DTOs

These will be implemented in `AuthenticatorController` and will replace the current logic for these routes.

### A. `POST /v1/authenticator/initiate-passkey-registration`

*   **Purpose:** Initiates the WebAuthn registration ceremony for the authenticated `MautUser`.
*   **Authentication:** Requires `X-Maut-Session-Token` header.
*   **Request Body (Optional):**
    *   `com.maut.core.modules.authenticator.dto.webauthn.InitiatePasskeyRegistrationServerRequestDto` (New DTO)
    *   If empty, `authenticatorAttachmentPreference` defaults to `"platform"`.
        ```json
        // Example (optional, can be empty for default "platform" preference)
        {
          // "authenticatorAttachmentPreference": "cross-platform"
        }
        ```
*   **Success Response (201 Created):**
    *   `com.maut.core.modules.authenticator.dto.webauthn.PublicKeyCredentialCreationOptionsDto` (New DTO)
    *   This DTO mirrors the `PublicKeyCredentialCreationOptions` object from the WebAuthn specification. All binary data (challenges, user IDs) will be Base64URL encoded.
        ```json
        {
          "rp": {
            "id": "app.maut.ai",
            "name": "Maut AI"
          },
          "user": {
            "id": "BASE64URL_ENCODED_BYTES_OF_MAUT_USER_UUID_STRING", // String representation of MautUser.id (UUID) -> bytes -> Base64URL
            "name": "maut_user_login_name", // e.g., MautUser.clientSystemUserId
            "displayName": "Maut User Friendly Name" // e.g., MautUser.clientSystemUserId
          },
          "challenge": "BASE64URL_ENCODED_CRYPTOGRAPHIC_CHALLENGE_BYTES",
          "pubKeyCredParams": [
            { "type": "public-key", "alg": -7 },  // ES256
            { "type": "public-key", "alg": -257 } // RS256
          ],
          "timeout": 60000,
          "attestation": "direct", // Consider "none" for simplicity if attestation is not strictly needed.
          "authenticatorSelection": {
            "authenticatorAttachment": "platform", // Default, can be overridden by request DTO
            "requireResidentKey": false,
            "userVerification": "preferred"
          },
          "excludeCredentials": [
            // {
            //   "type": "public-key",
            //   "id": "BASE64URL_ENCODED_EXISTING_CREDENTIAL_ID_BYTES",
            //   "transports": ["internal", "usb"]
            // }
          ]
        }
        ```

### B. `POST /v1/authenticator/complete-passkey-registration`

*   **Purpose:** Completes the WebAuthn registration using the authenticator's response.
*   **Authentication:** Requires `X-Maut-Session-Token` header.
*   **Request Body:**
    *   `com.maut.core.modules.authenticator.dto.webauthn.CompletePasskeyRegistrationServerRequestDto` (New DTO)
    *   Mirrors the JSON representation of `PublicKeyCredential` from `navigator.credentials.create()`. Binary data is Base64URL encoded.
        ```json
        {
          "id": "BASE64URL_ENCODED_NEW_CREDENTIAL_ID_BYTES",
          "rawId": "BASE64URL_ENCODED_NEW_CREDENTIAL_ID_BYTES",
          "type": "public-key",
          "response": { // AuthenticatorAttestationResponse
            "clientDataJSON": "BASE64URL_ENCODED_CLIENT_DATA_JSON_AS_BYTES",
            "attestationObject": "BASE64URL_ENCODED_ATTESTATION_OBJECT_AS_BYTES"
          },
          "clientExtensionResults": {},
          "authenticatorAttachment": "platform", // Optional: e.g., "platform" or "cross-platform"
          "friendlyName": "Chrome on Sam's MacBook" // Optional: User-provided or client-derived
        }
        ```
*   **Success Response (200 OK):**
    *   `com.maut.core.modules.authenticator.dto.webauthn.PasskeyRegistrationResultDto` (New DTO)
        ```json
        {
          "success": true,
          "credentialId": "BASE64URL_ENCODED_NEW_CREDENTIAL_ID_BYTES",
          "friendlyName": "Chrome on Sam's MacBook",
          "createdAt": "2025-05-15T11:30:00.000Z"
        }
        ```

## 3. Database Tables (New)

Migration scripts to be created using `bin/create_migration.sh`. Requires `uuid-ossp` extension or `gen_random_uuid()` function in PostgreSQL.

### A. `webauthn_registration_challenges`
Stores challenges for the registration ceremony. Short-lived records.

*   `id` (UUID PRIMARY KEY DEFAULT gen_random_uuid())
*   `maut_user_id` (UUID NOT NULL, REFERENCES `maut_users(id)` ON DELETE CASCADE) - Assuming `maut_users.id` is `UUID`.
*   `challenge` (TEXT NOT NULL) - Base64URL encoded.
*   `relying_party_id` (TEXT NOT NULL)
*   `expires_at` (TIMESTAMPTZ NOT NULL)
*   `created_at` (TIMESTAMPTZ NOT NULL DEFAULT `now()`)

### B. `maut_user_webauthn_credentials`
Stores registered WebAuthn credentials (passkeys).

*   `id` (UUID PRIMARY KEY DEFAULT gen_random_uuid())
*   `maut_user_id` (UUID NOT NULL, REFERENCES `maut_users(id)` ON DELETE CASCADE) - Assuming `maut_users.id` is `UUID`.
*   `external_id` (TEXT NOT NULL UNIQUE) - Base64URL encoded credential ID from authenticator.
*   `public_key_cose` (BYTEA NOT NULL) - COSE-encoded public key.
*   `signature_counter` (BIGINT NOT NULL)
*   `transports` (TEXT[] DEFAULT '{}')
*   `friendly_name` (TEXT)
*   `aaguid` (TEXT)
*   `attestation_type` (TEXT)
*   `created_at` (TIMESTAMPTZ NOT NULL DEFAULT `now()`)
*   `last_used_at` (TIMESTAMPTZ)

**Constraints/Indexes:**
*   `maut_user_webauthn_credentials`: Unique constraint on `(maut_user_id, external_id)`. Index on `maut_user_id`. Index on `external_id`.
*   `webauthn_registration_challenges`: Index on `(maut_user_id, challenge)` or `challenge`.

## 4. High-Level Server-Side Logic

### A. `initiate-passkey-registration`
1.  Identify `MautUser` (via `SessionService`).
2.  Generate secure random challenge (Base64URL encoded).
3.  Get RP & User Info (RP ID from config, User ID is Base64URL of `MautUser.id` (UUID string)).
4.  Store challenge, `MautUser.id`, RP ID in `webauthn_registration_challenges` with expiry.
5.  Build `PublicKeyCredentialCreationOptionsDto` (set `authenticatorAttachment` to `"platform"` if not specified in request).
6.  Return DTO.

### B. `complete-passkey-registration`
1.  Identify `MautUser`.
2.  Decode request (Base64URL fields).
3.  Retrieve stored challenge from `webauthn_registration_challenges` using `clientDataJSON.challenge` and `maut_user_id`. Validate.
4.  Verify `clientDataJSON` (`type`, `challenge`, `origin` against configured `webauthn.relyingPartyOrigins`).
5.  Verify `attestationObject` using a WebAuthn library (e.g., WebAuthn4J). This includes parsing, verifying `authData` (RP ID hash, flags), and the attestation statement. Extract `credentialId`, `publicKeyCose`, `signCount`, `aaguid`.
6.  Store credential in `maut_user_webauthn_credentials`. Delete used challenge.
7.  Return result DTO.

## 5. Key Configuration Parameters

To be added to `src/main/resources/config/application-config.json`:

```json
{
  // ... other configurations
  "webauthn": {
    "relyingPartyId": "app.maut.ai",
    "relyingPartyName": "Maut AI",
    "relyingPartyOrigins": [
      "https://app.maut.ai",
      "http://localhost:3001",
      "https://maut-ai-demo-app.vercel.app/"
    ]
    // Potentially add other settings like challenge TTL, preferred algorithms if needed
  }
}
```

## 6. Recommended Library

*   **WebAuthn4J** (`com.webauthn4j:webauthn4j-core`, `com.webauthn4j:webauthn4j-util`): For handling complex WebAuthn parsing, validation, and object creation.

