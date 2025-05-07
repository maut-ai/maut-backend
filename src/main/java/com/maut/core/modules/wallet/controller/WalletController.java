package com.maut.core.modules.wallet.controller;

import com.maut.core.modules.user.model.MautUser; 
import com.maut.core.modules.wallet.dto.EnrollWalletRequest;
import com.maut.core.modules.wallet.dto.EnrollWalletResponse;
import com.maut.core.modules.wallet.dto.WalletDetailsResponse;
import com.maut.core.modules.wallet.service.WalletService; 
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService; 

    @PostMapping("/enroll")
    public ResponseEntity<EnrollWalletResponse> enrollWallet(
        @Valid @RequestBody EnrollWalletRequest request) {
        
        MautUser mautUser = null; 
        EnrollWalletResponse response = walletService.enrollNewWallet(mautUser, request.getWalletDisplayName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/details")
    public ResponseEntity<WalletDetailsResponse> getWalletDetails() {
        MautUser mautUser = null; 
        WalletDetailsResponse response = walletService.getWalletDetails(mautUser);
        return ResponseEntity.ok(response);
    }
}
