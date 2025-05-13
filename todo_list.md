# Maut Backend To-Do List

- [x] **Task 18: Improve Duplicate Data Handling in Client Registration**
  - [x] 18.1: Create `EmailAlreadyExistsException` and `TeamNameAlreadyExistsException`.
  - [x] 18.2: Modify `AuthService.registerClient()` to check for existing email and team name before saving, throwing custom exceptions.
  - [x] 18.3: Update `GlobalExceptionHandler` to handle new exceptions with 409 CONFLICT.
  - [x] 18.4: Run health check and verify.

- [ ] **Task 19: Enhance `/v1/auth/me` API Response**
  - [x] 19.1: Create `TeamMembershipInfoResponseDto` (teamId, teamName, userRoleName).
  - [x] 19.2: Create `AuthenticatedUserResponseDto` (userDetails without passwordHash, List<TeamMembershipInfoResponseDto>).
  - [ ] 19.3: Review and potentially update `User` entity's `teamMemberships` fetching strategy.
  - [ ] 19.4: Modify `AuthController.getCurrentUser()` to fetch team memberships and return `AuthenticatedUserResponseDto`.
  - [ ] 19.5: Update `docs/api_definitions.md` for `/v1/auth/me` with the new response structure.
  - [ ] 19.6: Run health check and verify.

- [ ] **Task 20: Refactor ClientApplication Module (API Key Functionality)**
  - [x] 20.1: Update `ClientApplication` Entity & DTOs
    - [x] Add `teamId` field to `ClientApplication.java`.
    - [x] Rename `clientName` to `name` in `ClientApplication.java`.
    - [x] Update `CreateClientApplicationRequest.java`: rename `clientName` to `name`.
    - [x] Update/Create DTOs (`MyClientApplicationResponse.java`/`ClientApplicationResponse.java`, and a detail DTO) to include `id`, `name`, `mautApiClientId`, `createdAt`, `enabled`, and `allowedOrigins` (for detail view).
  - [x] 20.2: Create Database Migration for `client_applications` Table
    - [x] Use `bin/create_migration.sh clientapplication "Modify client_applications for team ownership and name change"`
    - [x] Migration script to add `team_id UUID` (with FK to `teams(id)`) and rename `client_name` to `name`.
  - [x] 20.3: Refactor `ClientApplicationAdminController` to `ClientApplicationController`
    - [x] Rename file and class from `ClientApplicationAdminController` to `ClientApplicationController`.
    - [x] Update `@RequestMapping` from `/v1/admin/client-applications` to `/v1/clientapplication`.
    - [x] Adjust controller method authentication to be accessible by authenticated team members.
  - [x] 20.4: Update `ClientApplicationService`
    - [x] Modify `createClientApplication` method to accept `teamId` (from authenticated `User`) and use the renamed `name` field.
    - [x] Update methods for listing and fetching client applications to filter by the authenticated user's team.
    - [x] Address `User` to `Team` linking (how to get `Team` from `User` for service logic).
    - [ ] Add `allowedOrigins` to `CreateClientApplicationRequest` if it should be set at creation time.
  - [x] 20.5: Implement/Update API Endpoints in `ClientApplicationController`
    - [x] `POST /`: Create client application for the authenticated user's team.
    - [x] `GET /`: List client applications for the authenticated user's team (including fields: `id`, `name`, `mautApiClientId`, `createdAt`, `enabled`).
    - [x] `GET /{clientApplicationId}`: Get details of a specific client application (including `allowedOrigins`).
  - [x] 20.6: Run `bin/start_and_healthcheck.sh` and verify functionality.
    - [x] Fix any linting or startup errors.
    - [x] Verify application starts and health check passes.
  - [x] 20.7: Update API Documentation (`docs/api_definitions.md` or OpenAPI spec).

- [ ] **Task 21: Client Application Admin Endpoints (`ClientApplicationAdminController`)**

- [ ] **Task 22: Fix Lint Errors in SessionController and SessionService**
  - [x] Fix lint errors in `SessionController.java` and `SessionService.java` by resolving missing imports.

- [x] **Task 23: Enhance Create Client Application API**
  - [x] Include `client_secret` in the response of `POST /v1/clientapplication`.
  - [x] Ensure `client_secret` is *only* returned upon creation and not in other responses (e.g., Get Details).

## General Tasks
- [x] Update API documentation in `docs/api_definitions.md` for `ClientApplicationController` new endpoints (Create, List, Get Details) and ensure correct placement.
