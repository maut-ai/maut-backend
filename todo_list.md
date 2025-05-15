# Maut Backend TODO List

## Phase 1: Core Setup & Initial Turnkey Integration (Wallet Enrollment)

### 1. Project Setup & Basic Structure
- [x] Initialize Spring Boot project with necessary dependencies (Web, JPA, Security, PostgreSQL, Lombok, Jackson).
- [x] Configure basic application properties (server port, database connection).
- [x] Set up basic package structure (e.g., `com.maut.core.controller`, `com.maut.core.service`, `com.maut.core.repository`, `com.maut.core.model`, `com.maut.core.config`, `com.maut.core.external`).
- [x] Implement basic health check endpoint (`/status`).

### 2. Database Setup & Migrations
- [x] Integrate Flyway for database migrations.
- [x] Create initial migration script for core tables (e.g., `users`, `wallets`, `organizations`). (Schema TBD, placeholder for now)

### 3. User Authentication & Authorization (Basic)
- [x] Implement basic JWT-based authentication.
- [x] Create `User` entity and repository.
- [x] Create `AuthService` for user registration and login.
- [x] Secure relevant endpoints.

### 4. Turnkey API Integration - Foundation
- [x] Define Turnkey API configuration properties (API key, secret, base URL, organization ID).
- [x] Create `RestTemplate` bean for Turnkey API calls.
- [x] Create `TurnkeyService` interface and `TurnkeyServiceImpl` for interacting with Turnkey.
  - [x] Add method signature for `createSubOrganization(String subOrganizationName)` -> `TurnkeySubOrganization`.
  - [x] Add method signature for `createMautManagedPrivateKey(String subOrganizationId, String privateKeyName)` -> `TurnkeyPrivateKey`.
  - [x] Add method signature for `createUserControlledPrivateKey(String subOrganizationId, String userId)` -> `TurnkeyPrivateKey`.
- [x] Create DTOs for Turnkey API requests and responses:
  - **Sub-organization Creation:**
    - [x] `CreateSubOrganizationRequest`
    - [x] `SubOrganizationParameters`
    - [x] `RootUserPayload`
    - [x] `TurnkeyActivityResponseWrapper` (generic for various activity responses)
    - [x] `ActivityResponsePayload` (generic for various activity results)
    - [x] `CreateSubOrgActivityResult`
    - [x] `SubOrganizationDetails`
  - **Private Key Creation (Maut-Managed & User-Controlled have similar structures):**
    - [x] `CreatePrivateKeysRequest`
    - [x] `CreatePrivateKeysParameters`
    - [x] `PrivateKeySpecification` (name, algorithm, curve, tags)
    - [x] `CreatePrivateKeysActivityResult`
    - [x] `CreatePrivateKeysResultV2Payload`
    - [x] `PrivateKeyDetails` (id, name, status, algorithm, curve, addresses)
    - [x] `Address` (format, address)
- [x] Implement `TurnkeyAuthenticator` utility for `X-Stamp` header generation (HMAC-SHA256).
- [x] Implement `createSubOrganization` in `TurnkeyServiceImpl`.
  - [x] Construct request payload.
  - [x] Make POST request to `/public/v1/submit/create_sub_organization`.
  - [x] Parse response and extract `subOrganizationId`.
  - [x] Handle errors and logging.
- [x] Implement `createMautManagedPrivateKey` in `TurnkeyServiceImpl`.
  - [x] Construct request payload.
  - [x] Make POST request to `/public/v1/submit/create_private_keys`.
  - [x] Parse response and extract `privateKeyId` and `address`.
  - [x] Handle errors and logging.

### 5. Wallet Enrollment Business Logic
- [x] Create `WalletService` and `WalletController`.
- [x] Implement `enrollNewMautManagedWallet(String userId, String organizationName, String privateKeyName)` method in `WalletService`.
  - [x] Check if an organization (sub-organization in Turnkey) with `organizationName` already exists for the Maut user.
    - [x] If not, call `TurnkeyService.createSubOrganization(organizationName)` to create it. Store the mapping.
    - [x] If yes, retrieve its `subOrganizationId`.
  - [x] Call `TurnkeyService.createMautManagedPrivateKey(subOrganizationId, privateKeyName)`.
  - [x] Store wallet details (e.g., `userId`, `turnkeyPrivateKeyId`, `turnkeySubOrganizationId`, `address`, `type = MAUT_MANAGED`) in the Maut database.
  - [x] Return relevant wallet information to the caller.
- [x] Implement `enrollNewUserControlledWallet(String userId, String organizationName, String userControlledKeyIdentifier)` method (details TBD, depends on how user-controlled keys are handled with Turnkey).
  - [x] This might involve `TurnkeyService.createUserControlledPrivateKey` or a different flow.
  - [x] For now, this can be a stub.
- [x] Implement demo mode for `WalletService.enrollNewWallet` (local Ethereum key generation, bypass Turnkey).

### 6. API Endpoints for Wallet Enrollment
- [ ] Create `POST /api/v1/wallets/maut-managed` endpoint in `WalletController`.
  - [ ] Request body: `userId`, `organizationName`, `privateKeyName`.
  - [ ] Calls `WalletService.enrollNewMautManagedWallet`.
- [ ] Create `POST /api/v1/wallets/user-controlled` endpoint (placeholder).

### 7. Testing & Refinement
- [x] Fix compilation errors related to generics (`ActivityResponsePayload`, `TurnkeyActivityResponseWrapper`).
- [x] Fix `TurnkeyAuthenticator` resolution and `commons-codec` dependency.
- [x] Resolve duplicate method definitions in `TurnkeyServiceImpl`.
- [ ] Unit tests for `TurnkeyServiceImpl` (mocking `RestTemplate`).
- [ ] Unit tests for `WalletService` (mocking `TurnkeyService` and repositories).
- [ ] Integration tests for wallet enrollment flow (if feasible with H2/in-memory setup or testcontainers).
- [ ] Run `bin/start_and_healthcheck.sh` and ensure application starts and health check passes after Turnkey integration.

## Phase 2: Transaction Signing (Placeholder)
- ... (Details TBD) ...

## Phase 3: Other Core Features (Placeholder)
- ... (Details TBD) ...

## Vanilla WebAuthn Passkey Enrollment (Epic)

- [x] **Task 20: Add WebAuthn4J Maven Dependency**
  - Add `com.webauthn4j:webauthn4j-spring-security-core` to `pom.xml`.
- [x] **Task 21: Configure WebAuthn Settings**
  - Add `webauthn` configuration block to `src/main/resources/config/application-config.json` as per `docs/design/passkey_apis.md`.
- [x] **Task 22: Create WebAuthn Database Migrations**
  - Create Flyway migration scripts for `webauthn_registration_challenges` and `maut_user_webauthn_credentials` tables using `bin/create_migration.sh`.
  - Define table structures as per `docs/design/passkey_apis.md` (using UUIDs).
  - Ensure `uuid-ossp` or `gen_random_uuid()` is available/used for UUID generation.
  - Update `application.yml` to include the new migration script location if necessary (e.g., `classpath:db/modules/authenticator`).
- [x] **Task 23: Implement WebAuthn DTOs**
  - Create new DTOs in `com.maut.core.modules.authenticator.dto.webauthn`:
    - `InitiatePasskeyRegistrationServerRequestDto`
    - `PublicKeyCredentialCreationOptionsDto`
    - `CompletePasskeyRegistrationServerRequestDto`
    - `PasskeyRegistrationResultDto`
- [x] **Task 24: Implement WebAuthn Service Logic**
  - Update `AuthenticatorService` interface and `AuthenticatorServiceImpl`.
  - [x] Implement logic for `initiatePasskeyRegistration` using WebAuthn4J:
    - [x] Generate and store challenge in `webauthn_registration_challenges`.
    - [x] Construct and return `PublicKeyCredentialCreationOptionsDto`.
    - [x] Ensure `MautUser.id` (UUID) is correctly handled and Base64URL encoded for `user.id` in `PublicKeyCredentialCreationOptionsDto`.
    - [x] Refactor AuthenticatorSelectionCriteria logic based on client preferences or sensible defaults.
    - [x] **FIXED:** Ensure ApplicationConfig is correctly loading WebAuthn properties and accessible in AuthenticatorServiceImpl.
  - [ ] Implement logic for `completePasskeyRegistration` using WebAuthn4J:
    - [ ] Retrieve and validate challenge.
    - [ ] Verify attestation data and client data using WebAuthn4J.
    - [ ] Store new credential in `maut_user_webauthn_credentials`.
    - [ ] Delete used challenge.
- [ ] **Task 25: Update AuthenticatorController for WebAuthn**
  - Modify `AuthenticatorController` endpoints (`/initiate-passkey-registration`, `/complete-passkey-registration`) to use the new DTOs and service methods for vanilla WebAuthn.
- [ ] **Task 26: Test Vanilla WebAuthn Enrollment**
  - Run `bin/start_and_healthcheck.sh`.
  - Perform manual testing of the enrollment flow if possible, or write integration tests.

## Bugs / Issues to Address:
- (None currently identified after recent fixes)

## Notes / Decisions:
- Using Turnkey's sub-organizations to represent Maut organizations/tenants.
- Maut-managed keys are created directly via Turnkey API.
- User-controlled key flow needs further definition based on Turnkey capabilities for this model.
- `X-Stamp` header is crucial for Turnkey API authentication.
- Generic DTOs (`ActivityResponsePayload`, `TurnkeyActivityResponseWrapper`) improve reusability for different Turnkey activities.
- **Note:** Temporarily pivoted to a demo mode for `WalletService.enrollNewWallet` due to ongoing Turnkey authentication issues. This involves local Ethereum key generation and storage of the raw private key, bypassing actual Turnkey calls.

### Completed
- [x] CORE-001: Initial project setup and basic Spring Boot application structure.
- [x] CORE-002: Setup PostgreSQL database and integrate with Spring Data JPA.
- [x] WALLET-001: Implement demo mode for `WalletService.enrollNewWallet` (local Ethereum key generation, bypass Turnkey)
  - Note: This is a temporary implementation due to ongoing Turnkey authentication and sub-organization creation issues. The goal is to allow frontend development to proceed. Will require proper Turnkey integration later.
  - Updated to use DEMO-<uuid_substring> for `turnkeyUserPrivateKeyId` and `turnkeySubOrganizationId` to prevent collisions.
- [x] USER-MOD-001: Store Team for MautUser upon creation, based on the ClientApplication's owning Team. (Completed: 2025-05-14)
- [x] AUTH-FIX-001: Fix `ExpiredJwtException` for `/v1/auth/client/login` by adding `permitAll()` in `SecurityConfig`. (Completed: 2025-05-14)  Client-side will no longer send Auth header for login.
- [x] DEV-GUIDE-001: Add pagination parameter guidelines to `development_guidelines.md`. (Completed: 2025-05-14)
- [x] USER-API-001: Create paginated API to list MautUsers belonging to the logged-in user's team. (Completed: 2025-05-14)

### Future / Backlog
- [ ] Investigate and resolve Turnkey authentication and sub-organization creation issues.
