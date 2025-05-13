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
@RequestMapping("/api/v1/maut-user/authenticators") 
@RequiredArgsConstructor
@Slf4j
public class AuthenticatorController {

    private final AuthenticatorService authenticatorService;

    @PostMapping("/initiate-passkey-registration")
    public ResponseEntity<InitiatePasskeyRegistrationResponse> initiatePasskeyRegistration(
        // TODO: MautUser needs to be resolved from mautSessionToken or similar MautUser-specific auth mechanism
        /* @RequestHeader("X-Maut-Session-Token") String mautSessionToken */
    ) {
        MautUser mautUser = null; // Placeholder: Replace with actual MautUser resolved from its session/token
        // Example: MautUser mautUser = mautSessionService.validateAndGetMautUser(mautSessionToken);

        InitiatePasskeyRegistrationResponse response = authenticatorService.initiatePasskeyRegistration(mautUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/complete-passkey-registration")
    public ResponseEntity<CompletePasskeyRegistrationResponse> completePasskeyRegistration(
        // TODO: MautUser needs to be resolved from mautSessionToken or similar MautUser-specific auth mechanism
        /* @RequestHeader("X-Maut-Session-Token") String mautSessionToken, */
        @Valid @RequestBody CompletePasskeyRegistrationRequest request
    ) {
        MautUser mautUser = null; // Placeholder: Replace with actual MautUser resolved from its session/token

        CompletePasskeyRegistrationResponse response = authenticatorService.completePasskeyRegistration(mautUser, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("")
    public ResponseEntity<ListPasskeysResponse> listPasskeys(
        // TODO: MautUser needs to be resolved from mautSessionToken or similar MautUser-specific auth mechanism
        /* @RequestHeader("X-Maut-Session-Token") String mautSessionToken, */
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam(defaultValue = "0") int offset
    ) {
        MautUser mautUser = null; // Placeholder: Replace with actual MautUser resolved from its session/token
        
        ListPasskeysResponse response = authenticatorService.listPasskeys(mautUser, limit, offset);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{passkeyId}")
    public ResponseEntity<Void> deletePasskey(
        // TODO: MautUser needs to be resolved from mautSessionToken or similar MautUser-specific auth mechanism
        /* @RequestHeader("X-Maut-Session-Token") String mautSessionToken, */
        @PathVariable String passkeyId
    ) {
        MautUser mautUser = null; // Placeholder: Replace with actual MautUser resolved from its session/token

        authenticatorService.deletePasskey(mautUser, passkeyId);
        return ResponseEntity.noContent().build();
    }
}
