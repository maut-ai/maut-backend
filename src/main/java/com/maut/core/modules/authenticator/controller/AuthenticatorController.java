package com.maut.core.modules.authenticator.controller;

import com.maut.core.modules.authenticator.dto.CompletePasskeyRegistrationRequest;
import com.maut.core.modules.authenticator.dto.CompletePasskeyRegistrationResponse;
import com.maut.core.modules.authenticator.dto.InitiatePasskeyRegistrationResponse;
import com.maut.core.modules.authenticator.dto.ListPasskeysResponse;
import com.maut.core.modules.authenticator.service.AuthenticatorService;
import com.maut.core.modules.user.model.MautUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.core.annotation.AuthenticationPrincipal; // For Spring Security
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/authenticators")
@RequiredArgsConstructor
@Slf4j
public class AuthenticatorController {

    private final AuthenticatorService authenticatorService;

    @PostMapping("/initiate-passkey-registration")
    public ResponseEntity<InitiatePasskeyRegistrationResponse> initiatePasskeyRegistration(
        // @AuthenticationPrincipal MautUser mautUser // Placeholder for authenticated MautUser
    ) {
        // TODO: Replace null with actual authenticated MautUser from Spring Security context
        MautUser mautUser = null; // Placeholder

        InitiatePasskeyRegistrationResponse response = authenticatorService.initiatePasskeyRegistration(mautUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/complete-passkey-registration")
    public ResponseEntity<CompletePasskeyRegistrationResponse> completePasskeyRegistration(
        // @AuthenticationPrincipal MautUser mautUser, // Placeholder for authenticated MautUser
        @Valid @RequestBody CompletePasskeyRegistrationRequest request
    ) {
        // TODO: Replace null with actual authenticated MautUser from Spring Security context
        MautUser mautUser = null; // Placeholder

        CompletePasskeyRegistrationResponse response = authenticatorService.completePasskeyRegistration(mautUser, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("") // Maps to /v1/passkeys
    public ResponseEntity<ListPasskeysResponse> listPasskeys(
        // @AuthenticationPrincipal MautUser mautUser, // Placeholder for Spring Security
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam(defaultValue = "0") int offset
    ) {
        // TODO: Replace null with actual authenticated MautUser from Spring Security context
        MautUser mautUser = null; // Placeholder
        if (mautUser == null) {
            // This endpoint requires an authenticated user.
            // For now, service layer will handle null user if it's an issue.
            log.warn("Accessing /v1/passkeys without an authenticated user (using placeholder).");
        }
        ListPasskeysResponse response = authenticatorService.listPasskeys(mautUser, limit, offset);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{passkeyId}") // Maps to /v1/passkeys/{passkeyId}
    public ResponseEntity<Void> deletePasskey(
        // @AuthenticationPrincipal MautUser mautUser, // Placeholder for Spring Security
        @PathVariable String passkeyId
    ) {
        // TODO: Replace null with actual authenticated MautUser from Spring Security context
        MautUser mautUser = null; // Placeholder
        if (mautUser == null) {
            log.warn("Attempting to delete passkey {} without an authenticated user (using placeholder).", passkeyId);
            // Depending on security policy, this might be an error or handled by the service
        }

        authenticatorService.deletePasskey(mautUser, passkeyId);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }
}
