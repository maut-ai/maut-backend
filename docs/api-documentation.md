# Maut API Documentation

Welcome to the Maut API documentation! This document provides comprehensive information about the available endpoints and their usage.

## Authentication

### Client Authentication
- **POST `/v1/auth/client/register`**
  - Register a new client
  - Required: `ClientRegistrationRequest` body
  - Response: `ClientApplicationDetailResponse`
    ```json
    {
      "id": "UUID",
      "name": "string",
      "mautApiClientId": "string",
      "clientSecret": "string",
      "createdAt": "timestamp",
      "updatedAt": "timestamp",
      "enabled": boolean,
      "allowedOrigins": ["string"],
      "teamId": "UUID"
    }
    ```

- **POST `/v1/auth/client/login`**
  - Authenticate a client
  - Required: `LoginRequest` body
  - Response: `LoginResponse`
    ```json
    {
      "accessToken": "string"
    }
    ```

### Admin Authentication
- **POST `/v1/auth/admin/login`**
  - Authenticate an admin user
  - Required: `LoginRequest` body
  - Response: `LoginResponse`
    ```json
    {
      "accessToken": "string"
    }
    ```

### Current User
- **GET `/v1/auth/me`**
  - Get current authenticated user details
  - Response: `AuthenticatedUserResponseDto`
    ```json
    {
      "id": "UUID",
      "firstName": "string",
      "lastName": "string",
      "email": "string",
      "userType": "enum(UserType)",
      "isActive": boolean,
      "createdAt": "timestamp",
      "updatedAt": "timestamp",
      "teamMemberships": [
        {
          "teamId": "UUID",
          "teamName": "string",
          "userRoleName": "string"
        }
      ]
    }
    ```

## User Management

### Maut Users
- **GET `/v1/users`**
  - List MautUsers for the authenticated user's team
  - Returns paginated results
  - Response: `PaginatedMautUsersResponseDTO`
    ```json
    {
      "data": [
        {
          "id": "UUID",
          "mautUserId": "UUID",
          "clientSystemUserId": "string",
          "clientId": "string",
          "createdAt": "timestamp"
        }
      ],
      "position": number,
      "recordsTotal": number
    }
    ```

## Wallet Management

### Wallet Operations
- **POST `/v1/wallets/enroll`**
  - Enroll a new wallet
  - Required: `X-Maut-Session-Token` header, `EnrollWalletRequest` body
  - Response: `EnrollWalletResponse`
    ```json
    {
      "walletId": "string",
      "walletAddress": "string"
    }
    ```

- **GET `/v1/wallets/details`**
  - Get wallet details
  - Required: `X-Maut-Session-Token` header
  - Response: `WalletDetailsResponse`
    ```json
    {
      "walletId": "string",
      "displayName": "string",
      "walletAddress": "string",
      "turnkeySubOrganizationId": "string",
      "turnkeyMautPrivateKeyId": "string",
      "turnkeyUserPrivateKeyId": "string",
      "currentPolicy": {
        // policy details
      }
    }
    ```

## Policy Management

### Signing Policies
- **POST `/v1/policies/apply-signing-policy`**
  - Apply a signing policy
  - Required: `ApplySigningPolicyRequest` body
  - Response: `ApplySigningPolicyResponse`
    ```json
    {
      "status": "string",
      "turnkeyPolicyId": "string"
    }
    ```

## Activity Management

### Activity Operations
- **POST `/v1/activities/{activityId}/submit-user-approval`**
  - Submit user approval for an activity
  - Required: `SubmitUserApprovalRequest` body
  - Response: `SubmitUserApprovalResponse`
    ```json
    {
      "status": "string"
    }
    ```

- **GET `/v1/activities/{activityId}/status`**
  - Get activity status
  - Response: `ActivityStatusResponse`
    ```json
    {
      "activityId": "string",
      "status": "string",
      "activityType": "string",
      "result": {
        // activity result details
      }
    }
    ```

## Client Application Management

### Client Applications
- **POST `/v1/clientapplication`**
  - Create a new client application
  - Required: `CreateClientApplicationRequest` body
  - Response: `ClientApplicationDetailResponse`
    ```json
    {
      "id": "UUID",
      "name": "string",
      "mautApiClientId": "string",
      "clientSecret": "string",
      "createdAt": "timestamp",
      "updatedAt": "timestamp",
      "enabled": boolean,
      "allowedOrigins": ["string"],
      "teamId": "UUID"
    }
    ```

- **GET `/v1/clientapplication/{clientApplicationId}`**
  - Get client application details
  - Required: `clientApplicationId` path parameter
  - Response: `ClientApplicationDetailResponse`
    ```json
    {
      "id": "UUID",
      "name": "string",
      "mautApiClientId": "string",
      "clientSecret": "string",
      "createdAt": "timestamp",
      "updatedAt": "timestamp",
      "enabled": boolean,
      "allowedOrigins": ["string"],
      "teamId": "UUID"
    }
    ```

## Authenticator Management

### Passkey Operations
- **POST `/v1/authenticator/initiate-passkey-registration`**
  - Initiate passkey registration
  - Required: `X-Maut-Session-Token` header
  - Response: `InitiatePasskeyRegistrationResponse`
    ```json
    {
      "turnkeyChallenge": "string",
      "turnkeyAttestationRequest": {
        // attestation request details
      }
    }
    ```

- **GET `/v1/authenticator/list-passkeys`**
  - List registered passkeys
  - Required: `X-Maut-Session-Token` header
  - Response: `ListPasskeysResponse`
    ```json
    {
      "passkeys": [
        {
          "id": "string",
          "friendlyName": "string",
          "createdAt": "timestamp"
        }
      ]
    }
    ```

- **DELETE `/v1/authenticator/{passkeyId}`**
  - Delete a passkey
  - Required: `X-Maut-Session-Token` header, `passkeyId` path parameter
  - Response: `204 No Content`

## Role Management

### Admin Roles
- **POST `/v1/adminrole`**
  - Create a new admin role
  - Required: `CreateAdminRoleRequest` body
  - Requires `ADMIN_SUPER_ADMIN` authority
  - Response: `AdminRoleResponse`
    ```json
    {
      "id": "UUID",
      "name": "string",
      "description": "string",
      "permissions": ["string"]
    }
    ```

## Status

### Health Check
- **GET `/v1/status`**
  - Simple health check endpoint
  - Returns service status
  - Response:
    ```json
    {
      "status": "UP",
      "message": "Service is running normally"
    }
    ```

## Hello API

### Hello Messages
- **GET `/v1/hello`**
  - Get current hello message
  - Response: `HelloMessageDto`
    ```json
    {
      "id": "UUID",
      "message": "string",
      "updatedAt": "timestamp"
    }
    ```

- **POST `/v1/hello`**
  - Create a new hello message
  - Required: `HelloMessageDto` body
  - Response: `HelloMessageDto`
    ```json
    {
      "id": "UUID",
      "message": "string",
      "updatedAt": "timestamp"
    }
    ```

## Error Handling

All endpoints may return error responses in the following format:

```json
{
  "timestamp": "timestamp",
  "status": number,
  "error": "string",
  "message": "string",
  "path": "string"
}
```

Common error codes:
- 400 Bad Request: Invalid request format
- 401 Unauthorized: Authentication required
- 403 Forbidden: Insufficient permissions
- 404 Not Found: Resource not found
- 409 Conflict: Resource already exists
- 500 Internal Server Error: Unexpected error

## Security Notes

1. All endpoints require appropriate authentication
2. Sensitive endpoints are protected with role-based access control
3. Session tokens should be securely managed
4. API versioning is used (v1 prefix)
5. All responses include appropriate HTTP status codes

## Rate Limiting

Rate limiting is applied to prevent abuse of the API endpoints. The specific limits vary by endpoint and authentication level.

## Version History

- v1.0.0: Initial API version
