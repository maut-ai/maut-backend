# External Developer Services

This document outlines the services available to external developers, categorized for clarity.

## Authentication

Services related to user authentication and authorization.

### Endpoints

*   **/auth/login:** Allows users to log in and receive an authentication token.
*   **/auth/register:** Enables new users to create an account.
*   **/auth/refresh_token:** Provides a mechanism to refresh expired authentication tokens.

## Wallet Management

Services for managing user wallets, including balance inquiries and transaction history.

### Endpoints

*   **/wallets/balance:** Retrieves the current balance for a user's wallet.
*   **/wallets/transactions:** Fetches a list of transactions associated with a user's wallet.
*   **/wallets/transfer:** Allows users to transfer funds between wallets.

## User Management

Services for managing user accounts and profiles.

### Endpoints

*   **/users/profile:** Retrieves the profile information for the authenticated user.
*   **/users/profile/update:** Allows users to update their profile information.
*   **/users/activity:** Fetches a log of recent user activity.

## Data Services

Services providing access to market data and other relevant information.

### Endpoints

*   **/data/market_rates:** Retrieves current market exchange rates.
*   **/data/historical_prices:** Provides access to historical price data for various assets.

## Notification Services

Services for managing user notifications and alerts.

### Endpoints

*   **/notifications/preferences:** Allows users to set their notification preferences.
*   **/notifications/list:** Retrieves a list of recent notifications for the user.
