package com.maut.core.modules.authenticator.controller;

import com.maut.core.modules.authenticator.dto.CompletePasskeyRegistrationRequest;
import com.maut.core.modules.authenticator.dto.CompletePasskeyRegistrationResponse;
import com.maut.core.modules.authenticator.dto.ListPasskeysResponse;
import com.maut.core.modules.authenticator.dto.webauthn.InitiatePasskeyRegistrationServerRequestDto;
import com.maut.core.modules.authenticator.dto.webauthn.PasskeyRegistrationResultDto;
import com.maut.core.modules.authenticator.dto.webauthn.PublicKeyCredentialCreationOptionsDto;
import com.maut.core.modules.authenticator.dto.webauthn.CompletePasskeyRegistrationServerRequestDto;
import com.maut.core.modules.authenticator.service.AuthenticatorService;
import com.maut.core.modules.session.service.SessionService;
import com.maut.core.modules.user.model.MautUser; 
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/authenticator") 
@RequiredArgsConstructor
@Slf4j
public class AuthenticatorController {

    private final AuthenticatorService authenticatorService;
    private final SessionService sessionService;

    @PostMapping("/initiate-passkey-registration")
    public ResponseEntity<PublicKeyCredentialCreationOptionsDto> initiatePasskeyRegistration(
        @RequestHeader("X-Maut-Session-Token") String mautSessionToken,
        @RequestBody(required = false) InitiatePasskeyRegistrationServerRequestDto requestDto // Optional client hints
    ) {
        MautUser mautUser = sessionService.validateMautSessionTokenAndGetMautUser(mautSessionToken);
        PublicKeyCredentialCreationOptionsDto options = authenticatorService.initiateVanillaPasskeyRegistration(mautUser, requestDto);
        return ResponseEntity.ok(options);
    }

    @PostMapping("/complete-passkey-registration")
    public ResponseEntity<PasskeyRegistrationResultDto> completePasskeyRegistration(
        @RequestHeader("X-Maut-Session-Token") String mautSessionToken,
        @RequestBody CompletePasskeyRegistrationServerRequestDto requestDto
    ) {
        MautUser mautUser = sessionService.validateMautSessionTokenAndGetMautUser(mautSessionToken);
        PasskeyRegistrationResultDto result = authenticatorService.completeVanillaPasskeyRegistration(mautUser, requestDto);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            // Consider appropriate error status codes based on the failure reason in result.getMessage()
            // For now, a generic 400 Bad Request or 500 Internal Server Error might be used.
            // For simplicity, returning 400 for client-side errors and 500 if it's a server-side processing issue.
            // This logic might be refined in the service layer or here based on specific error types.
            return ResponseEntity.badRequest().body(result); // Or .status(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping("/complete-passkey-registration-old")
    public ResponseEntity<CompletePasskeyRegistrationResponse> completePasskeyRegistrationOld(
        @RequestHeader("X-Maut-Session-Token") String mautSessionToken,
        @Valid @RequestBody CompletePasskeyRegistrationRequest request
    ) {
        MautUser mautUser = sessionService.validateMautSessionTokenAndGetMautUser(mautSessionToken);

        CompletePasskeyRegistrationResponse response = authenticatorService.completePasskeyRegistration(mautUser, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("")
    public ResponseEntity<ListPasskeysResponse> listPasskeys(
        @RequestHeader("X-Maut-Session-Token") String mautSessionToken,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam(defaultValue = "0") int offset
    ) {
        MautUser mautUser = sessionService.validateMautSessionTokenAndGetMautUser(mautSessionToken);
        
        ListPasskeysResponse response = authenticatorService.listPasskeys(mautUser, limit, offset);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{passkeyId}")
    public ResponseEntity<Void> deletePasskey(
        @RequestHeader("X-Maut-Session-Token") String mautSessionToken,
        @PathVariable String passkeyId
    ) {
        MautUser mautUser = sessionService.validateMautSessionTokenAndGetMautUser(mautSessionToken);

        authenticatorService.deletePasskey(mautUser, passkeyId);
        return ResponseEntity.noContent().build();
    }
}
