package com.maut.core.modules.wallet.service;

import com.maut.core.common.exception.TurnkeyOperationException;
import com.maut.core.common.exception.UserAlreadyHasWalletException;
import com.maut.core.modules.user.model.MautUser;
import com.maut.core.modules.wallet.dto.EnrollWalletResponse;
import com.maut.core.modules.wallet.dto.WalletDetailsResponse;
import com.maut.core.modules.wallet.model.UserWallet;
import com.maut.core.modules.wallet.repository.UserWalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final UserWalletRepository userWalletRepository;

    @Override
    @Transactional
    public EnrollWalletResponse enrollNewWallet(MautUser mautUser, String walletDisplayName) {
        if (mautUser == null) {
            log.error("MautUser cannot be null for wallet enrollment.");
            throw new IllegalArgumentException("Authenticated MautUser is required for wallet enrollment.");
        }
        log.info("Attempting to enroll a new DEMO wallet for MautUser ID: {}", mautUser.getId());

        if (userWalletRepository.existsByMautUser(mautUser)) {
            log.warn("MautUser ID: {} already has a wallet. Aborting enrollment.", mautUser.getId());
            throw new UserAlreadyHasWalletException("User already has an enrolled wallet.");
        }

        String newWalletAddress;
        String privateKeyHex;

        try {
            ECKeyPair ecKeyPair = Keys.createEcKeyPair();
            privateKeyHex = Numeric.toHexStringWithPrefix(ecKeyPair.getPrivateKey());
            newWalletAddress = "0x" + Keys.getAddress(ecKeyPair.getPublicKey());
            log.info("Generated new DEMO Ethereum wallet. Address: {}, MautUser ID: {}", newWalletAddress, mautUser.getId());

        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
            log.error("Error generating Ethereum key pair for MautUser ID: {}: {}", mautUser.getId(), e.getMessage(), e);
            throw new TurnkeyOperationException("Failed to generate demo Ethereum key pair: " + e.getMessage(), e);
        }

        UserWallet newUserWallet = new UserWallet();
        newUserWallet.setMautUser(mautUser);
        newUserWallet.setWalletAddress(newWalletAddress);
        newUserWallet.setWalletDisplayName(walletDisplayName != null ? walletDisplayName : "My Maut Demo Wallet");
        newUserWallet.setCreatedAt(Instant.now());
        newUserWallet.setUpdatedAt(Instant.now());

        String randomSuffix = UUID.randomUUID().toString().substring(0, 8); 
        newUserWallet.setTurnkeyMautPrivateKeyId(privateKeyHex); 
        newUserWallet.setTurnkeyUserPrivateKeyId("DEMO-" + randomSuffix);
        newUserWallet.setTurnkeySubOrganizationId("DEMO-" + randomSuffix);

        UserWallet savedWallet = userWalletRepository.save(newUserWallet); 
        log.info("Successfully enrolled and saved DEMO wallet with ID: {} for MautUser ID: {}", savedWallet.getId(), mautUser.getId());

        return new EnrollWalletResponse(savedWallet.getId().toString(), savedWallet.getWalletAddress());
    }

    @Override
    public WalletDetailsResponse getWalletDetails(MautUser mautUser) {
        if (mautUser == null) {
            log.error("MautUser cannot be null for fetching wallet details.");
            throw new IllegalArgumentException("Authenticated MautUser is required for fetching wallet details.");
        }
        log.info("Fetching wallet details for MautUser ID: {}", mautUser.getId());

        UserWallet userWallet = userWalletRepository.findByMautUser(mautUser)
            .stream().findFirst()
            .orElseThrow(() -> {
                log.warn("No UserWallet found for MautUser ID: {}. Cannot fetch details.", mautUser.getId());
                return new com.maut.core.common.exception.ResourceNotFoundException("UserWallet not found for user " + mautUser.getId());
            });

        Map<String, Object> currentPolicyPlaceholder = new HashMap<>();
        currentPolicyPlaceholder.put("policyName", "N/A (Demo Mode)");

        WalletDetailsResponse response = WalletDetailsResponse.builder()
            .walletId(userWallet.getId().toString())
            .displayName(userWallet.getWalletDisplayName())
            .walletAddress(userWallet.getWalletAddress())
            .turnkeySubOrganizationId(userWallet.getTurnkeySubOrganizationId())
            .turnkeyMautPrivateKeyId(userWallet.getTurnkeyMautPrivateKeyId())
            .turnkeyUserPrivateKeyId(userWallet.getTurnkeyUserPrivateKeyId())
            .currentPolicy(currentPolicyPlaceholder)
            .createdAt(userWallet.getCreatedAt())
            .build();

        log.debug("Returning wallet details for MautUser ID: {}: {}", mautUser.getId(), response);
        return response;
    }
}
