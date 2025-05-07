# Maut Backend To-Do List

## Phase 1: Core Backend API & Data Model Implementation (v0)

- [x] **Task 1:** Define overall project structure and create `com.maut.core.modules.projectspecification.v0_architecture_proposal.md`.
  - Status: Done
  - Deliverable: `com.maut.core.modules.projectspecification.v0_architecture_proposal.md`
- [x] **Task 2:** Implement Maut Platform Backend Data Models (Section 3 of `com.maut.core.modules.projectspecification.v0_project_spec.md`).
  - [x] ClientApplication Model (`com.maut.core.modules.clientapplication.model.ClientApplication`)
  - [x] MautUser Model (`com.maut.core.modules.user.model.MautUser`)
  - [x] UserWallet Model (`com.maut.core.modules.wallet.model.UserWallet`)
  - [x] UserAuthenticator Model (`com.maut.core.modules.authenticator.model.UserAuthenticator`)
- [ ] **Task 3:** Implement Maut Platform Backend API Definitions (Section 2 of `com.maut.core.modules.projectspecification.v0_project_spec.md`).
  - [x] POST /v1/session
  - [x] POST /v1/wallet/enroll (Core structure implemented; Turnkey/Auth placeholders)
  - [x] POST /v1/authenticators/initiate-passkey-registration (Core structure implemented; Turnkey/Auth placeholders)
  - [x] POST /v1/authenticators/complete-passkey-registration (Core structure implemented; Turnkey/Auth placeholders; DB migration for new fields created)
  - [x] POST /v1/policies/apply-signing-policy (Core structure implemented; Turnkey/Auth placeholders)
  - [x] GET /v1/wallet/details (Core structure implemented; Turnkey/Auth placeholders)
  - [x] POST /v1/transactions/initiate-signing (Core structure implemented; Turnkey/Auth placeholders)
  - [x] POST /v1/activities/:activityId/submit-user-approval (Core structure implemented; Turnkey/Auth placeholders)
  - [x] GET /v1/activities/:activityId/status (Core structure implemented; Turnkey/Auth placeholders)
  - [x] GET /v1/activities (placeholder logic implemented)
  - [x] GET /v1/passkeys (placeholder logic implemented)
  - [x] DELETE /v1/passkeys/:passkeyId (placeholder logic implemented)
- [x] **Task 4:** Set up initial database migrations for the new models using Flyway. (Core tables created: `client_applications`, `maut_users`, `user_authenticators`, `user_wallets`)
- [ ] **Task 5:** Write unit and integration tests for all new services and controllers.
    - [x] Unit tests for `AuthenticatorController.java` (Initial structure and basic tests created)
    - [ ] Unit tests for `AuthenticatorServiceImpl.java`
        - [x] `initiatePasskeyRegistration` (initial placeholder tests)
        - [x] `listPasskeys` (comprehensive tests)
        - [x] `deletePasskey` (tests for placeholder logic)
        - [x] `completePasskeyRegistration` (comprehensive tests for current logic)
        - [x] `verifyPasskeyAssertion` (Placeholder implementation in AuthenticatorServiceImpl done)
        - [ ] `findAndValidateUserAuthenticator` (Method needs implementation in AuthenticatorServiceImpl)
        - [ ] `deletePasskey` (update tests once Turnkey client is integrated - Turnkey integration pending)
        - [ ] `initiatePasskeyRegistration` (update tests once Turnkey client is integrated - Turnkey integration pending)
    - [ ] Unit tests for `ActivityController.java`
