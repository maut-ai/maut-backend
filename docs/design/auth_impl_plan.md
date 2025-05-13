## **Maut.ai Authentication Implementation Plan**

This plan outlines the steps to implement the authentication and authorization system for maut.ai based on the agreed-upon architecture. It's divided into three phases to manage complexity and deliver functionality incrementally.

**Phase 1: Backend Core Authentication & Authorization**

* **Goal:** Establish the foundational backend components for user management, team structures, login, and basic role-based access control using JWTs. Excludes MFA and password reset for now.  
* **Technologies:** Java, Spring Boot, Spring Security, Spring Data JPA, JWT Library, BCrypt, Database (PostgreSQL/MySQL).

**Tasks:**

1. **Setup Project:**  
   * Initialize Spring Boot project with necessary dependencies (Web, Security, JPA, DB Driver, Lombok, Validation, JWT library).  
   * Configure database connection.  
   * Set up basic project structure (controllers, services, repositories, entities, DTOs, config).  
2. **Implement Data Models (Entities & Repositories):**  
   * Create JPA entities based on Section 3 of the architecture document:  
     * User (with user\_type discriminator, password\_hash, is\_active, basic profile fields)  
     * AdminRole  
     * UserAdminRole (Join entity or @ManyToMany mapping)  
     * Team  
     * TeamRole  
     * TeamMembership (Crucial join table linking User, Team, and TeamRole)  
   * Create Spring Data JPA repositories for each entity (UserRepository, TeamRepository, TeamMembershipRepository, etc.).  
   * Seed the database with initial roles (admin\_roles, team\_roles).  
3. **Implement Core Services:**  
   * UserService: Handles user creation, retrieval, and validation logic. Include methods like findUserByEmail, createUser.  
   * TeamService: Handles team creation and retrieval. Include createTeam.  
   * TeamMembershipService: Manages linking users to teams with specific roles. Include addTeamMember.  
   * PasswordEncoder: Configure BCryptPasswordEncoder bean.  
4. **Implement Registration API:**  
   * **API Endpoint:** POST /api/auth/client/register  
   * **Controller:** Create AuthController.  
   * **Request DTO:** ClientRegistrationRequest (includes firstName, lastName, email, password, confirmPassword, teamName).  
   * **Logic:**  
     * Inject UserService, TeamService, TeamMembershipService, PasswordEncoder.  
     * Implement validation (email uniqueness, password match, password complexity rules, team name rules). Use Spring Validation annotations on the DTO.  
     * Use @Transactional annotation on the service method.  
     * Call services to hash password, create User (user\_type='CLIENT'), create Team, create TeamMembership (assigning 'ROLE\_OWNER').  
     * Return HTTP 201 on success, 400/409 on validation errors.  
5. **Implement JWT Service:**  
   * Create JwtService responsible for:  
     * Generating JWTs upon successful login (taking user details/roles as input).  
     * Validating incoming JWTs (checking signature, expiration).  
     * Extracting user details (email, ID, roles) from a valid token.  
   * Configure JWT secret key securely (environment variable/secrets manager).  
6. **Implement Login API & Spring Security Core:**  
   * **API Endpoints:**  
     * POST /api/auth/admin/login  
     * POST /api/auth/client/login  
   * **Request DTO:** LoginRequest (email, password).  
   * **Response DTO:** LoginResponse (accessToken).  
   * **Logic:**  
     * Implement UserDetailsService: Create a custom implementation (UserDetailsServiceImpl) that loads users by email from the UserRepository. It should fetch appropriate roles (admin roles or team memberships) and convert them into Spring Security GrantedAuthority objects. Handle both 'ADMIN' and 'CLIENT' user types. For clients, authorities might look like TEAM\_123\_ROLE\_OWNER.  
     * Configure AuthenticationManager bean.  
     * In AuthController, handle login requests:  
       * Authenticate using AuthenticationManager.authenticate().  
       * If successful, fetch user details.  
       * Generate JWT using JwtService.  
       * Return JWT in the response.  
     * Handle authentication failures (e.g., bad credentials) \-\> return HTTP 401\.  
7. **Configure Spring Security Filter Chain:**  
   * Create SecurityConfig class.  
   * Configure HttpSecurity:  
     * Disable CSRF (common for stateless JWT APIs) and session management (STATELESS).  
     * Permit access to public endpoints (/api/auth/\*\*).  
     * Require authentication for all other endpoints (/api/\*\*).  
     * Add a custom JWT filter (JwtAuthenticationFilter) before the standard UsernamePasswordAuthenticationFilter. This filter will:  
       * Extract the JWT from the Authorization header.  
       * Validate the token using JwtService.  
       * If valid, load user details using UserDetailsService.  
       * Create an Authentication object and set it in the SecurityContextHolder.  
8. **Implement Basic Authorization:**  
   * Enable global method security (@EnableGlobalMethodSecurity(prePostEnabled \= true)).  
   * Add basic @PreAuthorize annotations to placeholder endpoints for testing:  
     * e.g., @PreAuthorize("hasRole('ADMIN')") for an admin-only test endpoint.  
     * e.g., @PreAuthorize("isAuthenticated()") for a generic authenticated endpoint.  
     * *Note:* Complex team-based authorization (hasAuthority('TEAM\_OWNER\_' \+ \#teamId)) might be refined later but ensure the necessary role information is loaded by UserDetailsService.

**Phase 2: Frontend Integration**

* **Goal:** Build the Admin Panel and Client Dashboard UIs to interact with the Phase 1 backend APIs. Users should be able to register (clients), log in (both), and view basic protected content based on their role.  
* **Technologies:** Frontend framework (React, Angular, Vue, etc.), CSS (Tailwind CSS recommended), Fetch API/Axios.

**Tasks:**

1. **Setup Frontend Projects:**  
   * Set up separate projects/modules for Admin Panel and Client Dashboard.  
   * Choose and configure UI framework, routing, state management.  
2. **Implement Public Pages:**  
   * **Client Dashboard:**  
     * Registration Page: Form matching ClientRegistrationRequest DTO. Include client-side validation (mirroring backend rules where appropriate). Call POST /api/auth/client/register. Handle success/error responses.  
     * Login Page: Form for email/password. Call POST /api/auth/client/login. Store JWT securely upon success (e.g., localStorage, state management). Redirect to dashboard.  
   * **Admin Panel:**  
     * Login Page: Form for email/password. Call POST /api/auth/admin/login. Store JWT securely. Redirect to admin dashboard.  
3. **Implement Authenticated Routes/Layout:**  
   * Set up protected routes that require a valid JWT.  
   * Create an HTTP client/interceptor (e.g., Axios instance) that automatically adds the Authorization: Bearer \<token\> header to requests for protected endpoints.  
   * Handle token expiration / 401 errors globally (e.g., redirect to login).  
4. **Implement Basic Dashboards:**  
   * **Client Dashboard:**  
     * Basic landing page after login.  
     * Display logged-in user information (fetched from a /api/me endpoint \- see below).  
     * Placeholder for team management features.  
   * **Admin Panel:**  
     * Basic landing page after login.  
     * Placeholder for admin functions.  
5. **Implement Backend /api/me Endpoint:**  
   * **API Endpoint:** GET /api/users/me (or similar)  
   * **Logic:**  
     * Requires authentication.  
     * Retrieves the currently authenticated user's details from the SecurityContextHolder.  
     * Fetches full user details (including team memberships for clients) from the database.  
     * Returns a UserResponse DTO (excluding sensitive info like password hash).  
   * **Frontend:** Call this endpoint after login or on page load for authenticated areas to get user context.  
6. **Implement Logout:**  
   * Frontend button/link.  
   * Clear the stored JWT from the client-side.  
   * Redirect to the login page.  
   * (Optional Backend): Implement a token blocklist endpoint if immediate server-side invalidation is needed (adds complexity).

**Phase 3: MFA & Password Reset**

* **Goal:** Implement MFA setup/verification and password reset functionality on the backend and integrate them into the frontend flows.  
* **Technologies:** Backend (TOTP library, Email service integration), Frontend (UI for MFA setup, password reset forms).

**Tasks:**

1. **Backend \- MFA:**  
   * **Add TOTP Library:** Integrate dev.samstevens.totp or similar.  
   * **Update User Entity:** Ensure mfa\_enabled (boolean) and mfa\_secret (string, encrypted) fields exist.  
   * **MFA Setup API:**  
     * POST /api/mfa/setup: Generates a new TOTP secret, stores it (encrypted) temporarily. Returns the secret and a QR code data URI. Requires authentication.  
     * POST /api/mfa/verify: User submits a TOTP code from their authenticator app. Backend verifies it against the temporarily stored secret. If valid, permanently saves the encrypted secret to the user's record and sets mfa\_enabled \= true. Requires authentication.  
   * **MFA Disable API:**  
     * POST /api/mfa/disable: Requires authentication and potentially current password or a valid TOTP code. Sets mfa\_enabled \= false and clears the mfa\_secret.  
   * **Update Login Flow:**  
     * Modify POST /api/auth/{admin|client}/login logic: After password validation, if user.isMfaEnabled() is true, return a specific response (e.g., HTTP 200 with mfaRequired: true) instead of a JWT.  
     * **MFA Challenge API:** POST /api/auth/mfa-challenge: Takes email/userId (from initial login attempt state) and the TOTP code. Verifies the code against the user's stored mfa\_secret. If valid, *then* generate and return the JWT. Implement rate limiting.  
2. **Backend \- Password Reset:**  
   * **Add Password Reset Token Entity/Table:** PasswordResetToken (user\_id, token (hashed), expiry\_date).  
   * **Add Email Service:** Configure JavaMailSender and integrate with an email provider (e.g., SendGrid, Mailgun). Create email templates.  
   * **Request Reset API:**  
     * POST /api/auth/password-reset/request: Takes email. Finds user. Generates a secure random token. Hashes the token and stores it with expiry in password\_reset\_tokens. Sends an email to the user with a link containing the *raw* token (e.g., https://maut.ai/reset-password?token=...). Implement rate limiting.  
   * **Perform Reset API:**  
     * POST /api/auth/password-reset/confirm: Takes token, newPassword, confirmPassword. Validates passwords match, complexity rules. Finds token in DB by hashing the input token. Checks if valid (exists, not expired). If valid, updates the user's password\_hash, invalidates the token (delete or mark as used). Return success/failure.  
3. **Frontend \- MFA Integration:**  
   * **User Profile/Settings Page:** Add section for MFA management.  
     * Button to "Enable MFA". Calls POST /api/mfa/setup. Displays QR code and input for verification code. Calls POST /api/mfa/verify on submission.  
     * Display MFA status ("Enabled"/"Disabled").  
     * Button to "Disable MFA". Calls POST /api/mfa/disable. May require password/TOTP confirmation UI.  
   * **Update Login Flow:**  
     * If login API returns mfaRequired: true, show a new input field for the TOTP code.  
     * Submit the code (along with email/identifier from the first step) to POST /api/auth/mfa-challenge.  
     * Handle success (store JWT, redirect) or failure (show error).  
4. **Frontend \- Password Reset Integration:**  
   * **Forgot Password Page:** Input for email. Calls POST /api/auth/password-reset/request. Shows confirmation message.  
   * **Reset Password Page:** (Accessed via email link, e.g., /reset-password?token=...). Form for newPassword, confirmPassword. Extracts token from URL query parameter. Submits data to POST /api/auth/password-reset/confirm. Shows success/error message. Redirects to login on success.

This phased plan allows for building and testing core functionality before adding more complex security features and frontend interactions. Remember to include thorough testing (unit, integration, end-to-end) at each stage.