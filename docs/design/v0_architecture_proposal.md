# Maut Platform Backend - v0 Architecture Proposal

This document outlines the proposed architecture for the Maut Platform Backend, focusing on the API definitions and data models specified in `v0_project_spec.md` (Sections 2 and 3). The architecture adheres to the project's `development_guidelines.md` and the modular monolith structure defined in `README.md`.

## 1. Guiding Principles

- **Modularity:** APIs and data will be grouped into logical modules based on domain concepts (e.g., `session`, `wallet`, `authenticator`, `transaction`, `policy`, `activity`).
- **Layered Architecture:** Each module will follow the Controller-Service-Repository pattern.
- **Data Transfer Objects (DTOs):** DTOs will be used for API request/response payloads and for data transfer between layers.
- **Clear Naming:** All classes, methods, and packages will use clear, descriptive names.
- **RESTful APIs:** Endpoints will adhere to REST principles with versioning (e.g., `/v1/...`).
- **Database Migrations:** Flyway will be used for all schema changes.
- **Javadoc:** Comprehensive Javadoc will be provided for all public classes and methods.

## 2. Core Data Models (Entities)

Based on Section 3 of `v0_project_spec.md`, the following core JPA entities will be created. These will reside in a common `model` package or within their respective module's `model` package if tightly coupled.

Location: `com.maut.core.modules.[moduleName].model` or `com.maut.core.common.model` if shared across many modules.

1.  **`ClientApplication`**: Represents a client application that integrates with Maut.
    *   `id` (UUID, PK)
    *   `mautApiClientId` (String, Unique): The unique ID provided by Maut.
    *   `clientName` (String)
    *   `clientSecretHash` (String): For backend-to-backend auth if needed, or for validating tokens.
    *   `allowedOrigins` (List<String> or String): For CORS or security checks.
    *   `createdAt`, `updatedAt` (Timestamps)

2.  **`MautUser`**: Represents an end-user within the Maut system.
    *   `id` (UUID, PK)
    *   `mautUserId` (String, Unique, UUID recommended): Maut's internal user identifier.
    *   `clientApplication` (ManyToOne with `ClientApplication`): The client app this user belongs to.
    *   `clientSystemUserId` (String): The user's ID within the client application's system (e.g., from `clientAuthToken`'s `sub`).
    *   `createdAt`, `updatedAt` (Timestamps)
    *   `userWallets` (OneToMany with `UserWallet`)
    *   `userAuthenticators` (OneToMany with `UserAuthenticator`)

3.  **`UserWallet`**: Represents a user's MPC wallet.
    *   `id` (UUID, PK)
    *   `mautUser` (ManyToOne with `MautUser`)
    *   `walletAddress` (String, Unique): The blockchain address of the wallet.
    *   `walletDisplayName` (String, Optional)
    *   `turnkeySubOrganizationId` (String, Unique): ID from Turnkey for the sub-org.
    *   `turnkeyPrivateKeyId` (String, Unique): ID for the Maut-held private key share in Turnkey.
    *   `defaultPolicyId` (String, Optional): Turnkey Policy ID for the default signing policy.
    *   `createdAt`, `updatedAt` (Timestamps)

4.  **`UserAuthenticator`**: Represents a user's authenticator (e.g., Passkey).
    *   `id` (UUID, PK)
    *   `mautUser` (ManyToOne with `MautUser`)
    *   `authenticatorType` (Enum: `PASSKEY`, etc.)
    *   `turnkeyAuthenticatorId` (String, Unique): ID from Turnkey for this authenticator.
    *   `authenticatorName` (String, Optional): User-friendly name (e.g., "Sam's YubiKey").
    *   `createdAt`, `updatedAt` (Timestamps)

**Relationships Summary:**
*   `ClientApplication` --1:M-- `MautUser`
*   `MautUser` --1:M-- `UserWallet`
*   `MautUser` --1:M-- `UserAuthenticator`

## 3. Proposed Module Structure & API Implementation

The application will be structured into the following modules under `com.maut.core.modules`: `session`, `wallet`, `authenticator`, `policy`, `transaction`, and `activity`. Each module will contain `controller`, `service`, `repository`, `dto`, and potentially `model` sub-packages.

### 3.1. `session` Module

Handles user session creation and management.

*   **Directory:** `com.maut.core.modules.session`
*   **Controller:** `SessionController.java`
    *   `POST /v1/session`: `createSession(SessionRequestDto): SessionResponseDto`
*   **Service:** `SessionService.java`
    *   `createSession(SessionRequestDto)`: Validates `clientAuthToken`, finds/creates `MautUser`, establishes HTTP session.
*   **DTOs:**
    *   `SessionRequestDto`: `clientAuthToken` (String)
    *   `SessionResponseDto`: `mautUserId` (String), `isNewMautUser` (boolean)
*   **Repository:** `MautUserRepository` (likely shared or in a common `user` module if user management becomes complex, but for now, can be accessed by `SessionService`). `ClientApplicationRepository`.

### 3.2. `wallet` Module

Handles wallet enrollment and details retrieval.

*   **Directory:** `com.maut.core.modules.wallet`
*   **Controller:** `WalletController.java`
    *   `POST /v1/wallet/enroll`: `enrollWallet(EnrollWalletRequestDto, @AuthenticationPrincipal MautUser user): EnrollWalletResponseDto`
    *   `GET /v1/wallet/details`: `getWalletDetails(@AuthenticationPrincipal MautUser user): WalletDetailsResponseDto`
*   **Service:** `WalletService.java`
    *   `enrollWallet(EnrollWalletRequestDto, MautUser user)`: Orchestrates Turnkey calls for sub-org and private key creation. Saves `UserWallet`.
    *   `getWalletDetails(MautUser user)`: Retrieves `UserWallet` information.
*   **DTOs:**
    *   `EnrollWalletRequestDto`: `walletDisplayName` (String, Optional)
    *   `EnrollWalletResponseDto`: `walletAddress` (String), `turnkeySubOrganizationId` (String), `turnkeyPrivateKeyId` (String)
    *   `WalletDetailsResponseDto`: `address` (String), `walletDisplayName` (String, Optional)
*   **Repository:** `UserWalletRepository.java`
*   **External Service Client:** `TurnkeyApiClient.java` (or similar, for interactions with Turnkey API - could be a common client).

### 3.3. `authenticator` Module

Handles passkey registration.

*   **Directory:** `com.maut.core.modules.authenticator`
*   **Controller:** `AuthenticatorController.java`
    *   `POST /v1/authenticators/initiate-passkey-registration`: `initiatePasskeyRegistration(InitiatePasskeyRegistrationRequestDto, @AuthenticationPrincipal MautUser user): InitiatePasskeyRegistrationResponseDto`
    *   `POST /v1/authenticators/complete-passkey-registration`: `completePasskeyRegistration(CompletePasskeyRegistrationRequestDto, @AuthenticationPrincipal MautUser user): CompletePasskeyRegistrationResponseDto`
*   **Service:** `AuthenticatorService.java`
    *   `initiatePasskeyRegistration(InitiatePasskeyRegistrationRequestDto, MautUser user)`: Calls Turnkey to get a challenge for WebAuthn.
    *   `completePasskeyRegistration(CompletePasskeyRegistrationRequestDto, MautUser user)`: Calls Turnkey to register the passkey, saves `UserAuthenticator`.
*   **DTOs:**
    *   `InitiatePasskeyRegistrationRequestDto`: `authenticatorName` (String, Optional) (Potentially `userId` from Turnkey if needed for their API)
    *   `InitiatePasskeyRegistrationResponseDto`: `challenge` (String), `turnkeyUserId` (String) (or whatever Turnkey returns)
    *   `CompletePasskeyRegistrationRequestDto`: `attestation` (Object/String based on WebAuthn response), `turnkeyChallengeResponse` (Object/String based on what Turnkey expects)
    *   `CompletePasskeyRegistrationResponseDto`: `authenticatorId` (String - Maut's ID), `turnkeyAuthenticatorId` (String)
*   **Repository:** `UserAuthenticatorRepository.java`
*   **External Service Client:** `TurnkeyApiClient.java`

### 3.4. `policy` Module

Handles applying signing policies.

*   **Directory:** `com.maut.core.modules.policy`
*   **Controller:** `PolicyController.java`
    *   `POST /v1/policies/apply-signing-policy`: `applySigningPolicy(ApplySigningPolicyRequestDto, @AuthenticationPrincipal MautUser user): ApplySigningPolicyResponseDto`
*   **Service:** `PolicyService.java`
    *   `applySigningPolicy(ApplySigningPolicyRequestDto, MautUser user)`: Calls Turnkey to create and apply the 2-of-2 signing policy for the user's wallet/sub-org.
*   **DTOs:**
    *   `ApplySigningPolicyRequestDto`: `userWalletId` (Long or `walletAddress` String), `turnkeySubOrganizationId` (String), `turnkeyUserAuthenticatorId` (String), `turnkeyMautPrivateKeyId` (String) (details needed to construct the policy in Turnkey)
    *   `ApplySigningPolicyResponseDto`: `policyId` (String - Turnkey policy ID), `status` (String)
*   **Repository:** (May not need direct repository access if it only orchestrates Turnkey calls and updates `UserWallet` via `WalletService`)
*   **External Service Client:** `TurnkeyApiClient.java`

### 3.5. `transaction` Module

Handles initiating the signing process.

*   **Directory:** `com.maut.core.modules.transaction`
*   **Controller:** `TransactionController.java`
    *   `POST /v1/transactions/initiate-signing`: `initiateSigning(InitiateSigningRequestDto, @AuthenticationPrincipal MautUser user): InitiateSigningResponseDto`
*   **Service:** `TransactionService.java`
    *   `initiateSigning(InitiateSigningRequestDto, MautUser user)`: Calls Turnkey to initiate the signing activity (Maut's 1st approval).
*   **DTOs:**
    *   `InitiateSigningRequestDto`: `type` ('RAW' | 'TEMPLATED'), `payload` (Object: e.g., `{ "unsignedTransaction": "0x...", "to": "...", "value": "...", "data": "..." }` or `{ "templateId": "...", "parameters": {} }`), `privateKeyId` (String, Optional - Turnkey private key ID)
    *   `InitiateSigningResponseDto`: `activityId` (String - Turnkey activity ID), `status` (String)
*   **External Service Client:** `TurnkeyApiClient.java`

### 3.6. `activity` Module

Handles user approval submission and status polling for signing activities.

*   **Directory:** `com.maut.core.modules.activity`
*   **Controller:** `ActivityController.java`
    *   `POST /v1/activities/{activityId}/submit-user-approval`: `submitUserApproval(@PathVariable String activityId, SubmitUserApprovalRequestDto approvalDto, @AuthenticationPrincipal MautUser user): SubmitUserApprovalResponseDto`
    *   `GET /v1/activities/{activityId}/status`: `getActivityStatus(@PathVariable String activityId, @AuthenticationPrincipal MautUser user): ActivityStatusResponseDto`
*   **Service:** `ActivityService.java`
    *   `submitUserApproval(String activityId, SubmitUserApprovalRequestDto approvalDto, MautUser user)`: Calls Turnkey to submit the user's approval (Passkey signature for the activity).
    *   `getActivityStatus(String activityId, MautUser user)`: Calls Turnkey to get the status of the signing activity.
*   **DTOs:**
    *   `SubmitUserApprovalRequestDto`: `signedChallenge` (String or Object, containing the Passkey's assertion/signature for the Turnkey activity)
    *   `SubmitUserApprovalResponseDto`: `status` (String)
    *   `ActivityStatusResponseDto`: `status` (String), `signedTx` (String, Optional), `txHash` (String, Optional)
*   **External Service Client:** `TurnkeyApiClient.java`

## 4. Common Components

Located under `com.maut.core.common`:

*   **`config`**: `JsonPropertySourceFactory`, Security configurations (e.g., for validating `clientAuthToken` and managing Maut user sessions/authentication principals).
*   **`exception`**: Global exception handlers (`@ControllerAdvice`), custom exception classes (e.g., `ResourceNotFoundException`, `TurnkeyApiException`, `InvalidTokenException`).
*   **`client` (or `external`)**: `TurnkeyApiClient.java` - A dedicated client for all Turnkey interactions. This client will handle API calls, error mapping, and potentially retry logic.
*   **`model` (optional):** If some models are truly shared across most/all modules and not specific enough to one.

## 5. Database Migrations (Flyway)

*   Initial schema setup in `src/main/resources/db/migration` (e.g., `V1__Initial_Schema.sql`) for core tables like `client_applications`, `maut_users`.
*   Module-specific migrations in `src/main/resources/db/modules/[moduleName]` for tables like `user_wallets`, `user_authenticators` (e.g., `V1.1__Create_User_Wallets.sql`).
*   Flyway locations will need to be updated in `application.yml` to include these new module migration paths.

## 6. Next Steps (Post-Approval)

1.  Create the directory structure for the new modules.
2.  Implement the JPA entities and their repositories.
3.  Set up initial Flyway migration scripts for these entities.
4.  Implement the DTOs for each API endpoint.
5.  Develop the `TurnkeyApiClient` for interacting with Turnkey.
6.  Implement the Services and Controllers for each module, starting with `session` and `wallet` enrollment flow.
7.  Write unit and integration tests.

This proposal provides a foundational structure. Details within DTOs and specific Turnkey interactions will be refined during implementation based on Turnkey's API documentation.
