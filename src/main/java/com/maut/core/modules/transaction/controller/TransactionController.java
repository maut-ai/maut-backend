package com.maut.core.modules.transaction.controller;

import com.maut.core.modules.transaction.dto.InitiateSigningRequest;
import com.maut.core.modules.transaction.dto.InitiateSigningResponse;
import com.maut.core.modules.transaction.service.TransactionService;
import com.maut.core.modules.user.model.MautUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.core.annotation.AuthenticationPrincipal; // For Spring Security
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/initiate-signing")
    public ResponseEntity<InitiateSigningResponse> initiateSigning(
        // @AuthenticationPrincipal MautUser mautUser, // Placeholder for authenticated MautUser
        @Valid @RequestBody InitiateSigningRequest request
    ) {
        // TODO: Replace null with actual authenticated MautUser from Spring Security context
        MautUser mautUser = null; // Placeholder

        InitiateSigningResponse response = transactionService.initiateSigning(mautUser, request);
        return ResponseEntity.ok(response);
    }
}
