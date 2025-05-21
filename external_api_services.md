# External API Services Documentation

This document outlines the various API services available to external developers, categorized for ease of understanding and integration. Each service category includes a list of specific endpoints and their functionalities.

## 1. Authentication Services

Provides endpoints for user authentication, registration, and session management. These services are crucial for securing access to user-specific data and functionalities.

### Endpoints:

*   **`/auth/register`**
    *   **Description:** Allows new users to create an account by providing necessary registration details (e.g., email, password). Returns a user identifier upon successful registration.
    *   **Methods:** `POST`
*   **`/auth/login`**
    *   **Description:** Enables registered users to log in using their credentials. Returns an access token and a refresh token upon successful authentication.
    *   **Methods:** `POST`
*   **`/auth/refresh-token`**
    *   **Description:** Allows users to obtain a new access token using a valid refresh token, typically when the current access token has expired.
    *   **Methods:** `POST`
*   **`/auth/logout`**
    *   **Description:** Invalidates the user's current session and access token.
    *   **Methods:** `POST`

## 2. User Management Services

Endpoints for managing user profiles, settings, and related information.

### Endpoints:

*   **`/users/me`**
    *   **Description:** Retrieves the profile information of the currently authenticated user (e.g., user ID, email, name, registration date).
    *   **Methods:** `GET`
*   **`/users/me/profile`**
    *   **Description:** Allows the authenticated user to update their profile information (e.g., name, contact details).
    *   **Methods:** `PUT`
*   **`/users/me/settings`**
    *   **Description:** Retrieves the current settings for the authenticated user (e.g., notification preferences, security settings).
    *   **Methods:** `GET`
*   **`/users/me/settings`**
    *   **Description:** Allows the authenticated user to update their settings.
    *   **Methods:** `PUT`

## 3. Wallet Services

Provides functionalities related to managing digital wallets, including balance inquiries, transaction history, and fund transfers.

### Endpoints:

*   **`/wallets`**
    *   **Description:** Retrieves a list of wallets associated with the authenticated user.
    *   **Methods:** `GET`
*   **`/wallets/{wallet_id}/balance`**
    *   **Description:** Gets the current balance for a specific wallet.
    *   **Methods:** `GET`
*   **`/wallets/{wallet_id}/transactions`**
    *   **Description:** Retrieves the transaction history for a specific wallet. Supports pagination and filtering by date or transaction type.
    *   **Methods:** `GET`
*   **`/wallets/{wallet_id}/transfer`**
    *   **Description:** Initiates a fund transfer from the user's wallet to another wallet or external address. Requires details like recipient address and amount.
    *   **Methods:** `POST`

## 4. Market Data Services

Endpoints for accessing real-time and historical market data, such as cryptocurrency prices, exchange rates, and trading volumes.

### Endpoints:

*   **`/market/prices`**
    *   **Description:** Retrieves the latest prices for specified cryptocurrencies or trading pairs.
    *   **Methods:** `GET`
*   **`/market/historical-data/{symbol}`**
    *   **Description:** Fetches historical market data (e.g., OHLCV - Open, High, Low, Close, Volume) for a given symbol over a specified period.
    *   **Methods:** `GET`
*   **`/market/exchange-rates`**
    *   **Description:** Provides current exchange rates between different fiat currencies and cryptocurrencies.
    *   **Methods:** `GET`

## 5. Notification Services

Allows users to manage their notification preferences and retrieve past notifications.

### Endpoints:

*   **`/notifications`**
    *   **Description:** Retrieves a list of notifications for the authenticated user. Supports pagination.
    *   **Methods:** `GET`
*   **`/notifications/preferences`**
    *   **Description:** Allows the authenticated user to view and update their notification preferences (e.g., enable/disable email notifications for certain events).
    *   **Methods:** `GET`, `PUT`
*   **`/notifications/{notification_id}/read`**
    *   **Description:** Marks a specific notification as read.
    *   **Methods:** `POST`

This document is based on the information extracted from `docs/api-documentation.md` and `docs/api_definitions.md`.
