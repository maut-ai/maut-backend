# Maut Backend To-Do List

- [x] **Task 18: Improve Duplicate Data Handling in Client Registration**
  - [x] 18.1: Create `EmailAlreadyExistsException` and `TeamNameAlreadyExistsException`.
  - [x] 18.2: Modify `AuthService.registerClient()` to check for existing email and team name before saving, throwing custom exceptions.
  - [x] 18.3: Update `GlobalExceptionHandler` to handle new exceptions with 409 CONFLICT.
  - [x] 18.4: Run health check and verify.
