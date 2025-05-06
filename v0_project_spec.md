# **Maut SDK & Backend API Definitions for 2-of-2 MPC Flow**

This document outlines the key methods of the Maut Wallet SDK and the corresponding Maut Platform Backend APIs required to implement the detailed 2-of-2 MPC signing flow using a user's Passkey and a Maut backend key with Turnkey.

### **Section 1: Maut Wallet SDK Method Definitions**

These are the public methods exposed by the Maut Wallet SDK to the Client Application.

**1. Initialize**

```typescript
initialize(config: { 
  mautApiClientId: string, 
  mautBackendUrl: string 
}): Promise<void>
```

* **Purpose:** Initializes the SDK with necessary configuration. Sets up communication with the Maut Backend.  
* **Parameters:**  
  * config: An object containing:  
    * mautApiClientId: The unique ID provided by Maut to the Client Application.  
    * mautBackendUrl: The base URL for the Maut Platform Backend API (e.g., https://api.maut.ai).  
* **Actions:**  
  * Stores the configuration.  
  * May perform initial health checks or fetch SDK configuration from the Maut Backend.  
* **External API Calls (Maut Backend):**  
  * None directly during initialization typically, but could fetch remote SDK config if designed that way.  
* **Returns:** A Promise that resolves when initialization is complete, or rejects on failure.

**2. Register Session With Token**

```typescript
registerSessionWithToken(clientAuthToken: string): Promise<{ 
  mautUserId: string, 
  isNewMautUser: boolean 
}>
```

* **Purpose:** Establishes an authenticated session with the Maut Platform Backend using a token provided by the Client Application's backend. This token proves the user has been authenticated by the Client Application.  
* **Parameters:**  
  * clientAuthToken: A short-lived, signed token (e.g., JWT) from the Client Application's backend, containing a unique user identifier within the client's system.  
* **Actions:**  
  * Sends the clientAuthToken to the Maut Backend to validate and establish a Maut-specific session (e.g., via secure cookies).  
* **External API Calls (Maut Backend):**  
  * POST /v1/session (with clientAuthToken).  
* **Returns:** A Promise that resolves with an object containing the mautUserId and a boolean isNewMautUser indicating if this user is new to Maut, or rejects on failure.

**3. Enroll Wallet**

```typescript
enrollWallet(params?: { 
  walletDisplayName?: string 
}): Promise<{ 
  walletAddress: string 
}>
```

* **Purpose:** Orchestrates the entire onboarding flow for a new user to create their Turnkey sub-organization, private key, register their Passkey, and apply the 2-of-2 signing policy. This method should only be called for users identified as isNewMautUser: true from registerSessionWithToken.  
* **Parameters:**  
  * params (Optional): An object containing:  
    * walletDisplayName (Optional): A user-friendly display name for the wallet.  
* **Actions:**  
  1. Calls Maut Backend to create Turnkey sub-organization and private key, passing the optional walletDisplayName.  
  2. Calls Maut Backend to initiate Passkey registration (get challenge).  
  3. Uses browser's WebAuthn API (navigator.credentials.create()) with the challenge to prompt the user to create a Passkey.  
  4. Calls Maut Backend to complete Passkey registration with the attestation data from WebAuthn.  
  5. Calls Maut Backend to apply the 2-of-2 signing policy.  
  6. Emits events like 'walletEnrollmentStarted', 'passkeyCreationRequired', 'walletEnrollmentComplete', 'walletEnrollmentFailed'.  
* **External API Calls (Maut Backend):**  
  * POST /v1/wallet/enroll  
  * POST /v1/authenticators/initiate-passkey-registration  
  * POST /v1/authenticators/complete-passkey-registration  
  * POST /v1/policies/apply-signing-policy  
* **Browser API Interactions:**  
  * navigator.credentials.create()  
* **Returns:** A Promise that resolves with an object containing the walletAddress, or rejects if any step fails.

**4. Get Wallet Details**

```typescript
getWalletDetails(): Promise<{ 
  address: string, 
  walletDisplayName?: string 
}>
```

* **Purpose:** Retrieves the user's wallet address and display name.  
* **Parameters:** None.  
* **Actions:**  
  * Calls Maut Backend to fetch wallet information.  
* **External API Calls (Maut Backend):**  
  * GET /v1/wallet/details  
* **Returns:** A Promise that resolves with wallet details, or rejects on failure.

**5. Sign Transaction**

```typescript
signTransaction(params: { 
  type: 'RAW' | 'TEMPLATED', 
  payload: object, 
  privateKeyId?: string 
}): Promise<{ 
  signedTx: string, 
  txHash?: string 
}>
```

* **Purpose:** Orchestrates the 2-of-2 signing process for a raw or templated transaction.  
* **Parameters:**  
  * params: An object containing:  
    * type: 'RAW' or 'TEMPLATED'.  
    * payload: If RAW, contains { to: string, value?: string, data?: string, unsignedTransaction: string }. If TEMPLATED, contains { templateId: string, parameters: Record\<string, any\> }. The unsignedTransaction (hex-string) for raw transactions is expected to be prepared by the client or Maut backend before this step if not directly constructed by initiate-signing.  
    * privateKeyId (Optional): The ID of the private key to use for signing. If not provided, a default key for the user might be assumed by the backend.  
* **Actions:**  
  1. Emits 'signingStarted'.  
  2. Calls Maut Backend to initiate signing (Maut's 1st approval, gets activityId).  
  3. Emits 'signingRequiresUserAction'.  
  4. Uses browser's WebAuthn API (navigator.credentials.get()) with a challenge (obtained implicitly via Turnkey's frontend SDK handling of the activity or explicitly if Maut Backend provides it) to prompt user for Passkey approval.  
  5. Calls Maut Backend to submit the user's Passkey approval.  
  6. Polls Maut Backend for the signing activity status until completion or failure.  
  7. Emits 'signingComplete' with the signed transaction or 'signingFailed' on error.  
* **External API Calls (Maut Backend):**  
  * POST /v1/transactions/initiate-signing  
  * POST /v1/activities/:activityId/submit-user-approval  
  * GET /v1/activities/:activityId/status (polling)  
* **Browser API Interactions:**  
  * navigator.credentials.get()  
* **Returns:** A Promise that resolves with the signedTx and an optional txHash (if broadcasted by Maut), or rejects on failure.

**Event Emitters/Listeners (as per specs.md):**

```typescript
on(eventName: string, callback: Function): void
off(eventName: string, callback: Function): void
```

The SDK will use an internal emit function to trigger events like:
- `'initializeSuccess'` 
- `'loginStateChange'`
- `'walletConnected'`
- `'walletEnrollmentStarted'`
- `'passkeyCreationRequired'`
- `'walletEnrollmentComplete'`
- `'walletEnrollmentFailed'`
- `'signingStarted'`
- `'signingRequiresUserAction'`
- `'signingComplete'`
- `'signingFailed'`
- `'error'`

### **Section 2: Maut Platform Backend API Definitions**

These are the server-side APIs hosted by Maut (e.g., on https://api.maut.ai) that the Maut Wallet SDK interacts with.

**1. POST /v1/session**

```http
POST /v1/session
```

* **Purpose:** Validates a clientAuthToken from a Client Application, establishes a Maut-specific user session, and identifies if the user is new or returning to Maut.  
* **Request Body:**  

```json
{
  "clientAuthToken": "string (JWT)"
}
```

* **Actions:**  
  1. Validates clientAuthToken (signature, issuer, audience, expiry) against pre-configured Client Application details.  
  2. Extracts the client's unique user ID from the token's sub claim.  
  3. Looks up or creates a Maut user record, linking it to the client's user ID and the mautApiClientId.  
  4. Establishes a secure HTTP-only cookie session for subsequent SDK requests.  
* **Turnkey Calls:** None directly.  
* **Responses:**  
  * **200 OK**  
  
```json
{
  "mautUserId": "string",
  "isNewMautUser": boolean,
  "mautSessionId": "string (optional, if not solely cookie-based)"
}
```

  * **400 Bad Request**: Invalid token format or missing fields.  
  
```json
{
  "error": "string (description of the error)"
}
```

  * **401 Unauthorized**: Token validation failed (invalid signature, expired, etc.).  
  
```json
{
  "error": "string (description of the error)"
}
```

  * **500 Internal Server Error**: Unexpected server error.  
  
```json
{
  "error": "string (description of the error)"
}
```

**2. POST /v1/wallet/enroll**

```http
POST /v1/wallet/enroll
```

* **Purpose:** For a new Maut user (authenticated via Maut session), creates their Turnkey sub-organization, a root user within it, and their primary private key. Stores Turnkey-specific identifiers internally.  
* **Request Body:** (Session authenticated via secure cookie)  

```json
{
  "walletDisplayName": "string (optional)"
}
```

* **Actions:**  
  1. Verifies active Maut session.  
  2. Calls Turnkey API to create\_sub\_organization with a root user.  
  3. Calls Turnkey API to create\_private\_keys within the new sub-organization (potentially using walletDisplayName for Turnkey's privateKeyName).  
  4. Stores Turnkey's subOrganizationId, userId, privateKeyId, and derived walletAddress associated with the mautUserId in Maut's database.  
* **Turnkey Calls:**  
  * POST /public/v1/create\_sub\_organization  
  * POST /public/v1/create\_private\_keys  
* **Responses:**  
  * **201 Created**  
  
```json
{
  "mautUserId": "string",
  "walletAddress": "string"
}
```

  * **401 Unauthorized**: No active Maut session or invalid session.  
  * **409 Conflict**: Wallet/Turnkey resources already exist for this Maut user.  
  * **500 Internal Server Error**.

**3. POST /v1/authenticators/initiate-passkey-registration**

```http
POST /v1/authenticators/initiate-passkey-registration
```

* **Purpose:** Initiates the Passkey registration process for an authenticated Maut user by fetching a registration challenge from Turnkey. Necessary Turnkey identifiers are looked up by the backend based on the Maut session.  
* **Request Body:** (Session authenticated via secure cookie)  
  {}

* **Actions:**  
  1. Verifies active Maut session.  
  2. Looks up the user's turnkeyUserId and turnkeySubOrganizationId from Maut's database based on the mautUserId from the session.  
  3. Calls Turnkey API to create\_authenticators to get a challenge for Passkey registration.  
* **Turnkey Calls:**  
  * POST /public/v1/create\_authenticators (to request a challenge)  
* **Responses:**  
  * **200 OK**  
  
```json
{
  "challenge": "base64_encoded_string",
  "authenticatorId": "string"
}
```

  * **401 Unauthorized**.  
  * **404 Not Found**: Maut user or associated Turnkey user/sub-organization not found in Maut's DB.  
  * **500 Internal Server Error**.

**4. POST /v1/authenticators/complete-passkey-registration**

```http
POST /v1/authenticators/complete-passkey-registration
```

* **Purpose:** Completes the Passkey registration by sending the user's Passkey attestation data (obtained from the browser's WebAuthn API) to Turnkey.  
* **Request Body:** (Session authenticated via secure cookie)  

```json
{
  "authenticatorId": "string",
  // turnkeySubOrganizationId is looked up by the backend based on the Maut session
  "signedChallenge": {
    "rawId": "string (Base64URL encoded)",
    "response": {
      "clientDataJSON": "string (Base64URL encoded)",
      "attestationObject": "string (Base64URL encoded)"
    },
    "type": "public-key"
    // ... other fields from PublicKeyCredential
  }
}
```

* **Actions:**  
  1. Verifies active Maut session.  
  2. Looks up the user's turnkeySubOrganizationId from Maut's database.  
  3. Calls Turnkey API to register\_authenticator (or similar) using the provided authenticatorId and signedChallenge.  
* **Turnkey Calls:**  
  * POST /public/v1/register\_authenticator (or equivalent to submit attestation)  
* **Responses:**  
  * **200 OK**  
  
```json
{
  "status": "Passkey registered successfully"
}
```

  * **400 Bad Request**: Invalid attestation data or malformed request.  
  * **401 Unauthorized**.  
  * **500 Internal Server Error**.

**5. POST /v1/policies/apply-signing-policy**

```http
POST /v1/policies/apply-signing-policy
```

* **Purpose:** Applies the default 2-of-2 signing policy to the user's primary private key. Necessary Turnkey identifiers (privateKeyId, turnkeySubOrganizationId, turnkeyUserId) are looked up by the backend based on the Maut session.  
* **Request Body:** (Session authenticated via secure cookie)  
  {}

* **Actions:**  
  1. Verifies active Maut session.  
  2. Looks up the user's privateKeyId, turnkeySubOrganizationId, and turnkeyUserId from Maut's database based on the mautUserId from the session.  
  3. Constructs the 2-of-2 Turnkey policy object.  
  4. Calls Turnkey API to create\_policy and attach it.  
* **Turnkey Calls:**  
  * POST /public/v1/create\_policy  
* **Responses:**  
  * **200 OK**  
  
```json
{
  "status": "Policy applied successfully"
}
```

  * **401 Unauthorized**.  
  * **404 Not Found**: Relevant Maut or Turnkey resources (user, private key) not found based on session.  
  * **500 Internal Server Error**.

**6. GET /v1/wallet/details**

```http
GET /v1/wallet/details
```

* **Purpose:** Retrieves wallet address and display name for the authenticated Maut user.  
* **Request Parameters:** (Session authenticated via secure cookie)  
* **Actions:**  
  1. Verifies active Maut session.  
  2. Retrieves stored walletAddress and walletDisplayName for the mautUserId from Maut's database.  
* **Turnkey Calls:** None directly for this.  
* **Responses:**  
  * **200 OK**  
  
```json
{
  "address": "string (e.g., 0x...)",
  "walletDisplayName": "string (optional)"
}
```

  * **401 Unauthorized**.  
  * **404 Not Found**: Wallet not found for the user.  
  * **500 Internal Server Error**.

**7. POST /v1/transactions/initiate-signing**

```http
POST /v1/transactions/initiate-signing
```

* **Purpose:** Receives transaction details from the SDK, performs Maut's internal policy checks, and if approved, initiates the signing activity with Turnkey (providing Maut's first approval).  
* **Request Body:** (Session authenticated via secure cookie)  

```json
{
  "privateKeyId": "string (optional, backend can use default looked up via session)",
  "type": "RAW" | "TEMPLATED",
  "payload": {
    // If type == "RAW"
    // "to": "string (address)",
    // "value": "string (optional, hex wei)",
    // "data": "string (optional, hex)",
    // "unsignedTransaction": "string (hex-encoded unsigned transaction)"

    // If type == "TEMPLATED"
    // "templateId": "string",
    // "parameters": {}
  }
}
```

* **Actions:**  
  1. Verifies active Maut session.  
  2. If privateKeyId is not provided, looks up the default privateKeyId and turnkeySubOrganizationId for the user from Maut's database.  
  3. Performs Maut's internal policy engine checks (e.g., limits, permissions, fraud checks).  
  4. If Maut policy fails, return error.  
  5. If Maut policy passes, call Turnkey API sign\_transaction using Maut's platform API key (this is Maut's "1 of 2" approval). The unsignedTransaction must be part of the payload sent to Turnkey.  
* **Turnkey Calls:**  
  * POST /public/v1/sign\_transaction  
* **Responses:**  
  * **200 OK** (Maut Policy Pass)  
  
```json
{
  "activityId": "string",
  "status": "PENDING_USER_AUTHENTICATION"
}
```

  * **403 Forbidden** (Maut Policy Fail)  
  
```json
{
  "error": "Maut policy violation",
  "details": "string (optional, more specific reason)"
}
```

  * **400 Bad Request**: Invalid payload.  
  * **401 Unauthorized**.  
  * **500 Internal Server Error**.

**8. POST /v1/activities/:activityId/submit-user-approval**

```http
POST /v1/activities/:activityId/submit-user-approval
```

* **Purpose:** Submits the user's Passkey approval (assertion) to Turnkey for a pending signing activity.  
* **Path Parameter:** activityId: The ID of the signing activity.  
* **Request Body:** (Session authenticated via secure cookie)  

```json
{
  "signedChallenge": {
    "rawId": "string (Base64URL encoded)",
    "response": {
      "clientDataJSON": "string (Base64URL encoded)",
      "authenticatorAssertion": "string (Base64URL encoded)", // Note: field name might vary
      "signature": "string (Base64URL encoded)"
    },
    "type": "public-key"
    // ... other fields from PublicKeyCredential
  }
}
```

* **Actions:**  
  1. Verifies active Maut session.  
  2. Looks up necessary context like turnkeySubOrganizationId if required by Turnkey for the approval call, based on the Maut user or activity.  
  3. Calls Turnkey API to approve\_activity (or similar) with the activityId and the user's signed challenge.  
* **Turnkey Calls:**  
  * POST /public/v1/approve\_activity (or equivalent for submitting authenticator response)  
* **Responses:**  
  * **200 OK**  
  
```json
{
  "status": "User approval submitted"
  // Optionally, current activity status can be returned
  // "activityStatus": "PROCESSING" | "COMPLETED" | "FAILED"
}
```

  * **400 Bad Request**: Invalid assertion data.  
  * **401 Unauthorized**.  
  * **404 Not Found**: Activity not found or not in a state to be approved.  
  * **500 Internal Server Error**.

**9. GET /v1/activities/:activityId/status**

```http
GET /v1/activities/:activityId/status
```

* **Purpose:** Allows the SDK to poll for the status of a signing activity and retrieve the result (signed transaction) upon completion.  
* **Path Parameter:** activityId: The ID of the signing activity.  
* **Request Parameters:** (Session authenticated via secure cookie)  
* **Actions:**  
  1. Verifies active Maut session.  
  2. Looks up necessary context like turnkeySubOrganizationId if required by Turnkey for the get\_activity call, based on the Maut user or activity.  
  3. Calls Turnkey API get\_activity to fetch the current status and result of the activity.  
* **Turnkey Calls:**  
  * GET /public/v1/get\_activity  
* **Responses:**  
  * **200 OK** (Status: PENDING_USER_AUTHENTICATION / PROCESSING)  
  
```json
{
  "activityId": "string",
  "status": "PENDING_USER_AUTHENTICATION" | "PROCESSING"
}
```

  * **200 OK** (Status: COMPLETED)  
  
```json
{
  "activityId": "string",
  "status": "COMPLETED",
  "result": {
    "signedTransaction": "string (hex-encoded)"
  }
}
```

  * **200 OK** (Status: FAILED / REJECTED)  
  
```json
{
  "activityId": "string",
  "status": "FAILED" | "REJECTED",
  "error": {
    "code": "string (error code)",
    "message": "string (error message)"
  }
}
```

  * **401 Unauthorized**.  
  * **404 Not Found**: Activity not found.  
  * **500 Internal Server Error**.

### **Section 3: Maut Platform Backend Data Models**

These are conceptual data models for the Maut Platform Backend, representing how data might be structured in Maut's database to support the above APIs. The exact schema will depend on the chosen database technology.

**1. ClientApplication Model**

* **Purpose:** Stores information about Maut's clients (the applications integrating the Maut SDK).

```typescript
interface ClientApplication {
  mautApiClientId: string;         // Primary Key, Unique - The unique ID Maut provides to the client application
  clientName: string;              // Human-readable name of the client application (e.g., "Pond Inc.")
  clientTokenVerificationKey: string; // Public key or secret used to verify clientAuthToken signatures issued by this client
  allowedOrigins?: string[];      // Optional - CORS allowed origins for SDK communication
  status: string;                 // e.g., "active", "inactive", "suspended"
  createdAt: Date;
  updatedAt: Date;
}
```

**2. MautUser Model**

* **Purpose:** Represents an end-user within the Maut system, linking their identity across a client application and their Turnkey resources.

```typescript
interface MautUser {
  mautUserId: string;              // Primary Key, Unique, e.g., UUID generated by Maut
  clientApplicationId: string;     // Foreign Key referencing ClientApplication.mautApiClientId
  clientSystemUserId: string;      // The unique identifier for the user within the client's system
  turnkeySubOrganizationId?: string; // Unique, Nullable - The ID of the user's sub-organization in Turnkey
  turnkeyUserId?: string;         // Unique, Nullable - The ID of the user's root user entity in Turnkey
  status: string;                 // e.g., "pending_enrollment", "active", "suspended"
  createdAt: Date;
  updatedAt: Date;
}

// Indexes:
// Unique composite index on (clientApplicationId, clientSystemUserId)
```

**3. UserWallet Model**

* **Purpose:** Stores details about a specific wallet (private key) associated with a MautUser. A user might have multiple wallets over time or for different purposes, though the current flow focuses on one primary wallet.

```typescript
interface UserWallet {
  walletId: string;                // Primary Key, Unique, e.g., UUID generated by Maut
  mautUserId: string;              // Foreign Key referencing MautUser.mautUserId
  turnkeyPrivateKeyId: string;     // Unique - The ID of the private key in Turnkey
  walletAddress: string;           // Unique, Indexed - The blockchain address (e.g., Ethereum address)
  walletDisplayName?: string;      // Nullable - User-friendly display name for the wallet
  isDefault: boolean;              // Default: true - Indicates if this is the default wallet for the user
  createdAt: Date;
  updatedAt: Date;
}
```

**4. UserAuthenticator Model**

* **Purpose:** Stores information about the authenticators (specifically Passkeys in this flow) registered by a MautUser with Turnkey. This helps Maut keep track of registered authenticators if needed, although Turnkey is the source of truth.

```typescript
interface UserAuthenticator {
  authenticatorRecordId: string;    // Primary Key, Unique, e.g., UUID generated by Maut
  mautUserId: string;               // Foreign Key referencing MautUser.mautUserId
  turnkeyAuthenticatorId: string;   // Unique - The ID of the authenticator in Turnkey
  authenticatorType: string;        // e.g., "PASSKEY"
  registrationDate: Date;
  status: string;                  // e.g., "active", "revoked"
  createdAt: Date;
  updatedAt: Date;
}
```

**Relationships:**

```typescript
// Entity relationships:
// 1. A ClientApplication can have many MautUsers
// 2. A MautUser belongs to one ClientApplication
// 3. A MautUser can have one or more UserWallets (though the current flow implies one primary)
// 4. A MautUser can have one or more UserAuthenticators
```

This detailed outline should serve as a strong foundation for the development of the Maut Wallet SDK and its supporting backend infrastructure. Remember that exact Turnkey API endpoint names and payload structures should be verified against the official Turnkey API documentation.

This detailed outline should serve as a strong foundation for the development of the Maut Wallet SDK and its supporting backend infrastructure. Remember that exact Turnkey API endpoint names and payload structures should be verified against the official Turnkey API documentation.