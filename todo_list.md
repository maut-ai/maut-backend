# Maut Backend To-Do List - Authentication Implementation

## Phase 1: Backend Core Authentication & Authorization

- [x] **Task 1: Project Setup Review & Dependency Management**
  - [x] 1.1: Verify required dependencies are in `pom.xml` (Spring Web, Security, Data JPA, Lombok, Validation, JWT library - e.g., `io.jsonwebtoken:jjwt-api`, `jjwt-impl`, `jjwt-jackson`). Add if missing.
  - [x] 1.2: Confirm database connection details are configured (likely in `application-config.json` or `application.yml`).
  - [x] 1.3: Review existing project structure for alignment with plan (controllers, services, repositories, entities, DTOs, config).

- [x] **Task 2: Data Model Implementation**
  - [x] 2.1: Create `User` entity (`com.maut.core.modules.user.model.User`) with fields: `id`, `userType` (Enum: ADMIN, CLIENT), `firstName`, `lastName`, `email` (unique), `passwordHash`, `isActive`, `createdAt`, `updatedAt`.
    - *Note: Investigate/create a common `BaseEntity` for `id`, `createdAt`, `updatedAt` later.* 
  - [x] 2.2: Create `AdminRole` entity (`com.maut.core.modules.role.model.AdminRole`) with fields: `id`, `name` (e.g., 'ROLE_ADMIN', 'ROLE_SUPPORT').
  - [x] 2.3: Create `UserAdminRole` join entity/relation (`com.maut.core.modules.user.model.User`) for ManyToMany between `User` (ADMIN type) and `AdminRole`.
  - [x] 2.4: Create `Team` entity (`com.maut.core.modules.team.model.Team`) with fields: `id`, `name` (unique), `ownerUserId` (FK to User.id), `createdAt`, `updatedAt`.
  - [x] 2.5: Create `TeamRole` entity (`com.maut.core.modules.role.model.TeamRole`) with fields: `id`, `name` (e.g., 'ROLE_OWNER', 'ROLE_MEMBER', 'ROLE_READ_ONLY').
  - [x] 2.6: Create `TeamMembership` entity (`com.maut.core.modules.team.model.TeamMembership`) with fields: `id`, `user` (FK to User.id), `team` (FK to Team.id), `teamRole` (FK to TeamRole.id), `joinedAt`. Ensure composite unique constraint on (user, team).
  - [x] 2.7: Create JPA Repositories for all new entities (`UserRepository`, `AdminRoleRepository`, `TeamRepository`, `TeamRoleRepository`, `TeamMembershipRepository`). Extend `JpaRepository`. 
  - [x] 2.8: Create Flyway migration script (`db/migration/V{timestamp}__CreateAuthTables.sql`) to create tables for `User`, `AdminRole`, `user_admin_roles` (join table), `Team`, `TeamRole`, `TeamMembership`. Add constraints and indexes.
  - [x] 2.9: Create Flyway migration script (`db/migration/V{timestamp}__SeedRoles.sql`) to insert initial `AdminRole` and `TeamRole` records.
  - [x] 2.10: Create Flyway migration script (`db/migration/V{timestamp}__SeedRoles.sql`) to insert initial `AdminRole` and `TeamRole` records.
  - [x] 2.11: Resolve all schema validation errors by ensuring correct entity-to-column mappings.

- [x] **Task 3: Core Service Implementation**
  - [x] 3.1: Create `UserService` (`com.maut.core.modules.user.service.UserService`) with `@Service` annotation.
  - [x] 3.2: Implement `UserService.findUserByEmail(String email)` using `UserRepository`.
  - [x] 3.3: Implement `UserService.createUser(User user)` (handles saving).
  - [x] 3.4: Create `TeamService` (`com.maut.core.modules.team.service.TeamService`) with `@Service`.
  - [x] 3.5: Implement `TeamService.createTeam(Team team)`.
  - [x] 3.6: Create `TeamMembershipService` (`com.maut.core.modules.team.service.TeamMembershipService`) with `@Service`.
  - [x] 3.7: Implement `TeamMembershipService.addTeamMember(User user, Team team, TeamRole role)`.
  - [x] 3.8: Configure `BCryptPasswordEncoder` bean in a `SecurityConfig` or dedicated config class.

- [x] **Task 4: Client Registration API**
  - [x] 4.1: Create `AuthController` (`com.maut.core.modules.auth.controller.AuthController`) with `@RestController` and base path `/api/auth`.
  - [x] 4.2: Create `ClientRegistrationRequest` DTO (`com.maut.core.modules.auth.dto.ClientRegistrationRequest`) with validation annotations (`@NotBlank`, `@Email`, `@Size`, custom password complexity/match if needed). Include fields: `firstName`, `lastName`, `email`, `password`, `confirmPassword`, `teamName`.
  - [x] 4.3: Implement `POST /client/register` endpoint in `AuthController`. Inject services and `PasswordEncoder`.
  - [x] 4.4: Create a dedicated `AuthService` (`com.maut.core.modules.auth.service.AuthService`) to hold registration logic. Mark registration method with `@Transactional`.
  - [x] 4.5: Implement registration logic in `AuthService`: validate input (check email uniqueness, password match), hash password, create `User` (type CLIENT), create `Team` (set owner), find 'ROLE_OWNER' `TeamRole`, create `TeamMembership`.
  - [x] 4.6: Handle exceptions (e.g., `DataIntegrityViolationException` for duplicate email/team name) and return appropriate HTTP status codes (201, 400, 409).

- [x] **Task 5: Initial Health Check and Linting**
  - [x] 5.1: Run `bin/start_and_healthcheck.sh` to ensure the application starts, passes linting, and basic health checks.

- [x] **Task 6: JWT Service Implementation**
  - [x] 6.1: Create `JwtService` (`com.maut.core.modules.auth.service.JwtService`) with `@Service`.
  - [x] 6.2: Add JWT library dependency (if not done in 1.1).
  - [x] 6.3: Implement `JwtService.generateToken(UserDetails userDetails)`: create claims (subject=email, roles), set issued/expiration dates, sign with secret key.
  - [x] 6.4: Implement `JwtService.validateToken(String token)`: parse token, verify signature and expiration.
  - [x] 6.5: Implement `JwtService.extractUsername(String token)` (or `extractEmail`).
  - [x] 6.6: Implement `JwtService.extractClaims(String token)`.
  - [x] 6.7: Configure JWT secret key and expiration time securely via application properties/environment variables. Read them in `JwtService`.

- [x] **Task 7: Login API & Spring Security Core**
  - [x] 7.1: Create `LoginRequest` DTO (`com.maut.core.modules.auth.dto.LoginRequest`) with `email`, `password`.
  - [x] 7.2: Create `LoginResponse` DTO (`com.maut.core.modules.auth.dto.LoginResponse`) with `accessToken`.
  - [x] 7.3: Create `UserDetailsServiceImpl` (`com.maut.core.modules.auth.service.UserDetailsServiceImpl`) implementing `UserDetailsService`.
  - [x] 7.4: Implement `UserDetailsServiceImpl.loadUserByUsername(String email)`: Use `UserRepository` to find `User`. Fetch `AdminRoles` or `TeamMemberships` based on `userType`. Convert roles to `GrantedAuthority` objects (e.g., `SimpleGrantedAuthority`). Throw `UsernameNotFoundException` if user not found or inactive.
  - [x] 7.5: Define `AuthenticationManager` bean in `SecurityConfig` using `authenticationConfiguration.getAuthenticationManager()`.
  - [x] 7.6: Implement `POST /admin/login` endpoint in `AuthController`.
  - [x] 7.7: Implement `POST /client/login` endpoint in `AuthController`.
  - [x] 7.8: In login endpoints: Use `AuthenticationManager.authenticate()`. On success, get `UserDetails`, generate JWT using `JwtService`, return `LoginResponse`. Handle `BadCredentialsException` (return 401).

- [x] **Task 8: Spring Security Filter Chain Configuration**
  - [x] 8.1: Create `SecurityConfig` (`com.maut.core.config.SecurityConfig`) with `@Configuration`, `@EnableWebSecurity`.
  - [x] 8.2: Define `SecurityFilterChain` bean in `SecurityConfig`.
  - [x] 8.3: In `SecurityFilterChain`: Disable CSRF (`.csrf().disable()`).
  - [x] 8.4: Set session management to stateless (`.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)`).
  - [x] 8.5: Configure public endpoints: `/api/auth/**`, `GET /api/status`, `GET /api/hello` should be `permitAll()`. All other requests `authenticated()`.
  - [x] 8.6: Integrate `JwtAuthFilter` before `UsernamePasswordAuthenticationFilter` (`.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)`).
  - [x] 8.7: Define `AuthenticationProvider` bean: Use `DaoAuthenticationProvider`, set `UserDetailsService`, `PasswordEncoder`.

- [x] **Task 9: Basic Authorization Implementation** 
  - [x] 9.1: In `SecurityConfig`, add `@EnableGlobalMethodSecurity(prePostEnabled = true)` to enable method-level security.
  - [x] 9.2: Protect `POST /api/v1/admin/roles` endpoint in `AdminRoleController` to require `ADMIN_SUPER_ADMIN` authority using `@PreAuthorize("hasAuthority('ADMIN_SUPER_ADMIN')")`.
    - <!-- Note: AdminRoleController and POST /admin/roles endpoint do not exist yet. This task will be addressed when the controller is created. --> <!-- Controller created, path is /api/v1/admin/roles -->
  - [x] 9.3: Protect `GET /api/v1/client-applications/my` endpoint in `ClientApplicationController` to require `CLIENT` authority using `@PreAuthorize("hasAuthority('CLIENT')")`.
    - <!-- Note: ClientApplicationController and GET /client-applications/my endpoint exist and path is /api/v1/client-applications/my -->

- [x] **Task 10: Database Seeding for Test Users and Roles**

- [x] **Task 11: Document API Endpoints**
  - [x] 11.1: Gather all controller definitions and their DTOs.
  - [x] 11.2: Create `docs/api_definitions.md` with details for each endpoint (name, method, URL, description, example request/response).
