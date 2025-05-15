package com.maut.core.modules.authenticator.controller;

import com.maut.core.modules.authenticator.dto.CompletePasskeyRegistrationRequest;
import com.maut.core.modules.authenticator.dto.CompletePasskeyRegistrationResponse;
import com.maut.core.modules.authenticator.dto.ListPasskeysResponse;
import com.maut.core.modules.authenticator.dto.webauthn.InitiatePasskeyRegistrationServerRequestDto;
import com.maut.core.modules.authenticator.dto.webauthn.PublicKeyCredentialCreationOptionsDto;
import com.maut.core.modules.authenticator.service.AuthenticatorService;
import com.maut.core.modules.session.service.SessionService;
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
        @RequestBody(required = false) InitiatePasskeyRegistrationServerRequestDto requestDto
    ) {
        MautUser mautUser = sessionService.validateMautSessionTokenAndGetMautUser(mautSessionToken);

        PublicKeyCredentialCreationOptionsDto response = authenticatorService.initiateVanillaPasskeyRegistration(mautUser, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/complete-passkey-registration")
    public ResponseEntity<CompletePasskeyRegistrationResponse> completePasskeyRegistration(
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
