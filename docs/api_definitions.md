# API Endpoint Definitions

This document outlines the available API endpoints for the Maut backend system.

## Module: Activity

### Controller: `ActivityController`
Base Path: `/v1/activity` (Note: This controller currently references `MautUser` which, based on recent clarifications, should be resolved via a MautUser-specific session, not Spring Security `AuthenticationPrincipal`.)

---

#### 1. Submit User Approval for an Activity
- **Name:** Submit User Approval
- **HTTP Method:** `POST`
- **URL:** `/v1/activity/submit-user-approval`
- **Description:** Submits a user's approval (e.g., a signed challenge) for a specific activity.
- **Example Request (`application/json`):
  ```json
  {
    "signedChallenge": "_signed_challenge_string_goes_here_"
  }
  ```
- **Example Response (`application/json`):
  ```json
  {
    "status": "APPROVAL_SUBMITTED" 
  }
  ```

---

#### 2. Get Activity Status
- **Name:** Get Activity Status
- **HTTP Method:** `GET`
- **URL:** `/v1/activity/status`
- **Description:** Retrieves the current status and details of a specific activity.
- **Example Request:** (No request body, `activityId` in path)
- **Example Response (`application/json`):
  ```json
  {
    "activityId": "_activity_uuid_string_",
    "status": "PENDING_APPROVAL",
    "activityType": "TRANSACTION_SIGNING",
    "result": null
  }
  ```
  *Note: `result` can contain details if the activity is completed.*

---

#### 3. List Activities
- **Name:** List Activities
- **HTTP Method:** `GET`
- **URL:** `/v1/activity`
- **Description:** Lists activities for the authenticated `MautUser`, with optional filtering and pagination.
- **Query Parameters:**
    - `limit` (int, optional, default: 10): Number of activities to return.
    - `offset` (int, optional, default: 0): Offset for pagination.
    - `status` (String, optional): Filter activities by status (e.g., "PENDING_APPROVAL", "COMPLETED").
- **Example Request:** (No request body, parameters in URL query)
  `/v1/activity?limit=5&status=PENDING_APPROVAL`
- **Example Response (`application/json`):
  ```json
  {
    "activities": [
      {
        "activityId": "_activity_uuid_1_",
        "status": "PENDING_APPROVAL",
        "activityType": "TRANSACTION_SIGNING",
        "createdAt": "2023-05-15T10:00:00Z",
        "updatedAt": "2023-05-15T10:05:00Z",
        "resultSummary": null
      },
      {
        "activityId": "_activity_uuid_2_",
        "status": "COMPLETED",
        "activityType": "USER_AUTHENTICATION",
        "createdAt": "2023-05-14T09:00:00Z",
        "updatedAt": "2023-05-14T09:01:00Z",
        "resultSummary": "Authentication successful"
      }
    ],
    "limit": 5,
    "offset": 0,
    "totalActivities": 2
  }
  ```

## Module: Auth (Dashboard User Authentication)

### Controller: `AuthController`
Base Path: `/v1/auth`

---

#### 1. Register Client User
- **Name:** Register Client User
- **HTTP Method:** `POST`
- **URL:** `/v1/auth/client/register`
- **Description:** Registers a new client user (for dashboard access).
- **Example Request (`application/json`):
  ```json
  {
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "password": "Str0ngPa$$w0rd!",
    "confirmPassword": "Str0ngPa$$w0rd!",
    "teamName": "Doe's Company"
  }
  ```
- **Example Response:** 
  - `201 CREATED` (No body on success)
  - `4xx/5xx` with error message on failure.
    ```json
    // Example Error (e.g., 500 Internal Server Error)
    "Registration failed: Email already exists"
    ```

---

#### 2. Admin Login
- **Name:** Admin Login
- **HTTP Method:** `POST`
- **URL:** `/v1/auth/admin/login`
- **Description:** Authenticates an admin user (for dashboard access) and returns a JWT.
- **Example Request (`application/json`):
  ```json
  {
    "email": "admin@maut.com",
    "password": "adminpassword123"
  }
  ```
- **Example Response (`application/json`):
  ```json
  // Success (200 OK)
  {
    "accessToken": "_jwt_token_string_"
  }
  ```
  ```json
  // Unauthorized (401 Unauthorized)
  "User is not authorized for admin login."
  ```

---

#### 3. Client Login
- **Name:** Client Login
- **HTTP Method:** `POST`
- **URL:** `/v1/auth/client/login`
- **Description:** Authenticates a client user (for dashboard access) and returns a JWT.
- **Example Request (`application/json`):
  ```json
  {
    "email": "john.doe@example.com",
    "password": "Str0ngPa$$w0rd!"
  }
  ```
- **Example Response (`application/json`):
  ```json
  // Success (200 OK)
  {
    "accessToken": "_jwt_token_string_"
  }
  ```
  ```json
  // Unauthorized (401 Unauthorized)
  "User is not authorized for client login."
  ```

## Module: Authenticator (MautUser Passkeys)

### Controller: `AuthenticatorController`
Base Path: `/v1/authenticator` (Note: These endpoints require a `MautUser` to be identified, likely via a `mautSessionToken` header, which is not yet fully implemented in the controller methods shown.)

---

#### 1. Initiate Passkey Registration
- **Name:** Initiate Passkey Registration
- **HTTP Method:** `POST`
- **URL:** `/v1/authenticator/initiate-passkey-registration`
- **Description:** Initiates the passkey registration process for the authenticated `MautUser`.
- **Example Request:** (No request body, assumes `MautUser` identified from session/token)
- **Example Response (`application/json`):
  ```json
  {
    "turnkeyChallenge": "_challenge_string_from_turnkey_",
    "turnkeyAttestationRequest": {
      // Structure defined by Turnkey/WebAuthn PublicKeyCredentialCreationOptions
      "rp": {
        "name": "Your Client App Name",
        "id": "your-relying-party-id.com"
      },
      "user": {
        "id": "_maut_user_id_base64url_",
        "name": "user_identifier_for_passkey",
        "displayName": "User Display Name"
      },
      "challenge": "_challenge_string_from_turnkey_base64url_", // Same as turnkeyChallenge, but base64url
      "pubKeyCredParams": [
        { "type": "public-key", "alg": -7 }, // ES256
        { "type": "public-key", "alg": -257 } // RS256
      ],
      "timeout": 60000,
      "attestation": "direct",
      "authenticatorSelection": {
        "authenticatorAttachment": "platform",
        "userVerification": "required",
        "residentKey": "required"
      }
    }
  }
  ```

---

#### 2. Complete Passkey Registration
- **Name:** Complete Passkey Registration
- **HTTP Method:** `POST`
- **URL:** `/v1/authenticator/complete-passkey-registration`
- **Description:** Completes the passkey registration process using the attestation data from the client.
- **Example Request (`application/json`):
  ```json
  {
    "turnkeyAttestation": {
      // PublicKeyCredential as JSON from client, example structure:
      "id": "_credential_id_base64url_",
      "rawId": "_credential_id_base64url_",
      "response": {
        "clientDataJSON": "_client_data_json_base64url_",
        "attestationObject": "_attestation_object_base64url_"
      },
      "type": "public-key"
    },
    "turnkeyChallenge": "_challenge_string_from_initiate_step_",
    "clientDataJSON": "_client_data_json_base64url_", 
    "transports": ["internal", "usb"],
    "authenticatorName": "My Phone Passkey"
  }
  ```
- **Example Response (`application/json`):
  ```json
  {
    "status": "SUCCESS",
    "authenticatorId": "_maut_authenticator_uuid_string_",
    "turnkeyAuthenticatorId": "_turnkey_authenticator_id_string_",
    "message": "Passkey registered successfully."
  }
  ```

---

#### 3. List Passkeys
- **Name:** List Passkeys
- **HTTP Method:** `GET`
- **URL:** `/v1/authenticator`
- **Description:** Lists all registered passkeys for the authenticated `MautUser`.
- **Query Parameters:**
    - `limit` (int, optional, default: 10): Number of passkeys to return.
    - `offset` (int, optional, default: 0): Offset for pagination.
- **Example Request:** (No request body, parameters in URL query)
  `/v1/authenticator?limit=5`
- **Example Response (`application/json`):
  ```json
  {
    "passkeys": [
      {
        "id": "_authenticator_uuid_1_",
        "name": "MacBook Pro Touch ID",
        "credentialId": "_webauthn_credential_id_1_base64url_",
        "createdAt": "2023-05-10T12:00:00Z",
        "lastUsedAt": "2023-05-15T08:30:00Z",
        "type": "PLATFORM",
        "enabled": true
      },
      {
        "id": "_authenticator_uuid_2_",
        "name": "YubiKey 5C",
        "credentialId": "_webauthn_credential_id_2_base64url_",
        "createdAt": "2023-04-20T15:00:00Z",
        "lastUsedAt": null,
        "type": "CROSS_PLATFORM",
        "enabled": true
      }
    ],
    "limit": 5,
    "offset": 0,
    "totalPasskeys": 2
  }
  ```

---

#### 4. Delete Passkey
- **Name:** Delete Passkey
- **HTTP Method:** `DELETE`
- **URL:** `/v1/authenticator/{passkeyId}`
- **Description:** Deletes a specific passkey for the authenticated `MautUser`.
- **Example Request:** (No request body, `passkeyId` in path)
- **Example Response:** 
  - `204 No Content` (On success)

---

## Module: Client Application (Client User)

### Controller: `ClientApplicationController`
Base Path: `/v1/clientapplication`

---

#### 1. Get My Client Applications
- **Name:** Get My Client Applications
- **HTTP Method:** `GET`
- **URL:** `/v1/clientapplication/my`
- **Description:** Retrieves a list of client applications associated with the authenticated client user.
- **Permissions Required:** `CLIENT` authority (authenticated client user).
- **Example Request:** (No request body)
- **Example Response (`200 OK`, `application/json`):
  ```json
  {
    "applicationNames": [
      "My First App",
      "Another Test App"
    ],
    "message": "Successfully retrieved applications."
  }
  ```
  *Note: The structure of `applicationNames` might evolve to include more details per application.* 
- **Error Responses:**
  - `401 UNAUTHORIZED` / `403 FORBIDDEN`: If the user is not authenticated or does not have `CLIENT` authority.

---

## Module: Role (Admin)

### Controller: `AdminRoleController`
Base Path: `/v1/adminrole`

---

#### 1. Create Admin Role
- **Name:** Create Admin Role
- **HTTP Method:** `POST`
- **URL:** `/v1/adminrole`
- **Description:** Creates a new administrative role for dashboard users.
- **Permissions Required:** `ADMIN_SUPER_ADMIN` authority.
- **Example Request (`application/json`):
  ```json
  {
    "name": "CONTENT_MODERATOR"
  }
  ```
- **Example Response (`201 CREATED`, `application/json`):
  ```json
  {
    "id": "_new_role_uuid_string_",
    "name": "CONTENT_MODERATOR",
    "createdAt": "2023-10-27T11:00:00Z",
    "updatedAt": "2023-10-27T11:00:00Z"
  }
  ```
- **Error Responses:**
  - `400 BAD_REQUEST`: If `name` is blank or fails validation (e.g., too short/long).
  - `401 UNAUTHORIZED` / `403 FORBIDDEN`: If the user is not authenticated or lacks `ADMIN_SUPER_ADMIN` authority.
  - `409 CONFLICT`: If a role with the same name already exists.

---

## Module: Hello (Example/Test)

### Controller: `HelloController`
Base Path: `/v1/hello`

---

#### 1. Get Hello Message
- **Name:** Get Hello Message
- **HTTP Method:** `GET`
- **URL:** `/v1/hello`
- **Description:** Retrieves the current hello message. This is a simple endpoint, often used for testing.
- **Example Request:** (No request body)
- **Example Response (`200 OK`, `application/json`):
  ```json
  {
    "id": 1,
    "message": "Hello from Maut!",
    "updatedAt": "2023-10-27T10:00:00"
  }
  ```

---

#### 2. Create Hello Message
- **Name:** Create Hello Message
- **HTTP Method:** `POST`
- **URL:** `/v1/hello`
- **Description:** Creates or updates the hello message.
- **Example Request (`application/json`):
  ```json
  {
    "message": "A new hello message!"
  }
  ```
- **Example Response (`201 CREATED`, `application/json`):
  ```json
  {
    "id": 2,
    "message": "A new hello message!",
    "updatedAt": "2023-10-27T10:05:00"
  }
  ```
- **Error Responses:**
  - `400 BAD_REQUEST`: If the `message` is blank or fails validation.
    ```json
    // Example for blank message
    {
        // Standard Spring Boot error response structure
        "timestamp": "2023-10-27T12:34:56.789+00:00",
        "status": 400,
        "error": "Bad Request",
        "errors": [
            {
                "codes": [
                    "NotBlank.helloMessageDto.message",
                    "NotBlank.message",
                    "NotBlank.java.lang.String",
                    "NotBlank"
                ],
                "arguments": [
                    {
                        "codes": [
                            "helloMessageDto.message",
                            "message"
                        ],
                        "arguments": null,
                        "defaultMessage": "message",
                        "code": "message"
                    }
                ],
                "defaultMessage": "Message cannot be blank",
                "objectName": "helloMessageDto",
                "field": "message",
                "rejectedValue": "",
                "bindingFailure": false,
                "code": "NotBlank"
            }
        ],
        "message": "Validation failed for object='helloMessageDto'. Error count: 1",
        "path": "/v1/hello"
    }
    ```

---

## Module: Status (Health Check)

### Controller: `StatusController`
Base Path: `/v1/status`

---

#### 1. Get Service Status
- **Name:** Get Service Status
- **HTTP Method:** `GET`
- **URL:** `/v1/status`
- **Description:** Provides a simple health check endpoint for the service.
- **Example Request:** (No request body)
- **Example Response (`200 OK`, `application/json`):
  ```json
  {
    "status": "UP",
    "message": "Service is running normally"
  }
  ```

---

## Module: Policy (MautUser)

### Controller: `PolicyController`
Base Path: `/v1/policy`

---

#### 1. Apply Signing Policy
- **Name:** Apply Signing Policy
- **HTTP Method:** `POST`
- **URL:** `/v1/policy/apply-signing-policy`
- **Description:** Applies a signing policy to a `MautUser`'s Turnkey entities. The specific `MautUser` is expected to be identified from their session (e.g., via `mautSessionToken`).
- **Example Request (`application/json`):
  ```json
  {
    "policyName": "My Organization's Default Policy",
    "policyDetails": {
      "name": "My Organization's Default Policy",
      "description": "Requires 2 of 3 approvals from trusted devices.",
      "effect": "ALLOW",
      "condition": "# ≥ 2",
      "obligations": [
        {
          "type": "DEVICE_TRUST",
          "parameters": {
            "minDeviceTrustLevel": "SECURE"
          }
        }
      ]
    }
  }
  ```
- **Example Response (`200 OK`, `application/json`):
  ```json
  {
    "status": "SUCCESS",
    "turnkeyPolicyId": "_turnkey_policy_uuid_string_"
  }
  ```
- **Error Responses:**
  - `400 BAD_REQUEST`: If `policyName` is blank, or `policyDetails` are null/empty or malformed.
  - `401 UNAUTHORIZED`: If the `MautUser` cannot be identified or authenticated.
  - `5xx`: Server-side errors during policy application with Turnkey.

---

## Module: Role (Admin)

### Controller: `AdminRoleController`
Base Path: `/v1/adminrole`

---

#### 1. Create Admin Role
- **Name:** Create Admin Role
- **HTTP Method:** `POST`
- **URL:** `/v1/adminrole`
- **Description:** Creates a new administrative role for dashboard users.
- **Permissions Required:** `ADMIN_SUPER_ADMIN` authority.
- **Example Request (`application/json`):
  ```json
  {
    "name": "CONTENT_MODERATOR"
  }
  ```
- **Example Response (`201 CREATED`, `application/json`):
  ```json
  {
    "id": "_new_role_uuid_string_",
    "name": "CONTENT_MODERATOR",
    "createdAt": "2023-10-27T11:00:00Z",
    "updatedAt": "2023-10-27T11:00:00Z"
  }
  ```
- **Error Responses:**
  - `400 BAD_REQUEST`: If `name` is blank or fails validation (e.g., too short/long).
  - `401 UNAUTHORIZED` / `403 FORBIDDEN`: If the user is not authenticated or lacks `ADMIN_SUPER_ADMIN` authority.
  - `409 CONFLICT`: If a role with the same name already exists.

---

## Module: Session (MautUser)

### Controller: `SessionController`
Base Path: `/v1/session`

---

#### 1. Create MautUser Session
- **Name:** Create MautUser Session
- **HTTP Method:** `POST`
- **URL:** `/v1/session`
- **Description:** Processes a `clientAuthToken` (obtained from a client application's authentication system after the end-user logs in there) to establish a Maut session for the corresponding `MautUser`. It returns a `mautSessionToken` which is then used to authorize subsequent API calls for that `MautUser`.
- **Example Request (`application/json`):
  ```json
  {
    "clientAuthToken": "_opaque_token_from_client_application_auth_system_"
  }
  ```
- **Example Response (`200 OK`, `application/json`):
  ```json
  {
    "mautUserId": "_maut_user_uuid_string_",
    "isNewMautUser": false,
    "mautSessionToken": "_generated_maut_session_token_jwt_or_opaque_"
  }
  ```
- **Error Responses:**
  - `400 BAD_REQUEST`: If `clientAuthToken` is missing or blank.
  - `401 UNAUTHORIZED`: If the `clientAuthToken` is invalid, expired, or the client application is not recognized/authorized.
  - `5xx`: Internal server errors during session processing.

---

## Module: Transaction (MautUser)

### Controller: `TransactionController`
Base Path: `/v1/transaction`

---

#### 1. Initiate Signing
- **Name:** Initiate Transaction Signing
- **HTTP Method:** `POST`
- **URL:** `/v1/transaction/initiate-signing`
- **Description:** Initiates a transaction signing process for the authenticated `MautUser`. This creates an activity that the user will need to approve (e.g., via a passkey challenge or other configured policy).
- **Example Request (`application/json`):
  ```json
  {
    "transactionType": "ETH_TRANSFER",
    "transactionDetails": {
      "toAddress": "0xRecipientAddress...",
      "value": "1000000000000000000", // 1 ETH in wei
      "data": "0xOptionalData"
    },
    "turnkeyPolicyId": "_optional_policy_uuid_to_override_default_"
  }
  ```
- **Example Response (`200 OK`, `application/json`):
  ```json
  {
    "activityId": "_activity_uuid_string_for_signing_approval_",
    "status": "PENDING_USER_APPROVAL" 
  }
  ```
- **Error Responses:**
  - `400 BAD_REQUEST`: If `transactionType` is blank, `transactionDetails` are null/invalid, or `turnkeyPolicyId` is malformed.
  - `401 UNAUTHORIZED`: If the `MautUser` cannot be identified or authenticated.
  - `5xx`: Server-side errors during transaction initiation with Turnkey or activity creation.

---

## Module: Wallet (MautUser)

### Controller: `WalletController`
Base Path: `/v1/wallet`

---

#### 1. Enroll New Wallet
- **Name:** Enroll New Wallet
- **HTTP Method:** `POST`
- **URL:** `/v1/wallet/enroll`
- **Description:** Enrolls a new wallet for the authenticated `MautUser`. This typically involves creating a new sub-organization and private keys within Turnkey.
- **Example Request (`application/json`):
  ```json
  {
    "walletDisplayName": "My Primary Ethereum Wallet"
  }
  ```
- **Example Response (`201 CREATED`, `application/json`):
  ```json
  {
    "walletAddress": "_newly_generated_wallet_address_"
  }
  ```
- **Error Responses:**
  - `400 BAD_REQUEST`: If `walletDisplayName` is invalid (e.g., too long).
  - `401 UNAUTHORIZED`: If the `MautUser` cannot be identified or authenticated.
  - `409 CONFLICT`: If the user already has a wallet (assuming one wallet per user for now, this might change).
  - `5xx`: Server-side errors during wallet creation with Turnkey.

---

#### 2. Get Wallet Details
- **Name:** Get Wallet Details
- **HTTP Method:** `GET`
- **URL:** `/v1/wallet/details`
- **Description:** Retrieves details for the authenticated `MautUser`'s enrolled wallet.
- **Example Request:** (No request body)
- **Example Response (`200 OK`, `application/json`):
  ```json
  {
    "walletId": "_user_wallet_uuid_string_",
    "displayName": "My Primary Ethereum Wallet",
    "walletAddress": "_wallet_address_string_",
    "turnkeySubOrganizationId": "_turnkey_sub_org_id_",
    "turnkeyMautPrivateKeyId": "_turnkey_maut_private_key_id_",
    "turnkeyUserPrivateKeyId": "_turnkey_user_private_key_id_",
    "currentPolicy": {
      "name": "Default Wallet Policy",
      "description": "Standard transaction limits and approvals.",
      // ... other policy details
    }
  }
  ```
- **Error Responses:**
  - `401 UNAUTHORIZED`: If the `MautUser` cannot be identified or authenticated.
  - `404 NOT_FOUND`: If the `MautUser` does not have an enrolled wallet.
  - `5xx`: Server-side errors.

---