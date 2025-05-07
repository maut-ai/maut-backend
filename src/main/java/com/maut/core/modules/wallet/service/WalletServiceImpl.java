package com.maut.core.modules.wallet.service;

import com.maut.core.common.exception.ResourceNotFoundException; // Added import
// import com.maut.core.common.exception.TurnkeyOperationException; // Commented out as Turnkey logic is placeholder
import com.maut.core.common.exception.UserAlreadyHasWalletException;
import com.maut.core.modules.user.model.MautUser;
// import com.maut.core.modules.wallet.dto.EnrollWalletRequest; // Removed unused import
import com.maut.core.modules.wallet.dto.EnrollWalletResponse;
import com.maut.core.modules.wallet.dto.WalletDetailsResponse;
import com.maut.core.modules.wallet.model.UserWallet;
import com.maut.core.modules.wallet.repository.UserWalletRepository;
// import com.maut.core.external.turnkey.TurnkeyService; // To be created
// import com.maut.core.external.turnkey.model.TurnkeyPrivateKey;
// import com.maut.core.external.turnkey.model.TurnkeySubOrganization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final UserWalletRepository userWalletRepository;
    // private final TurnkeyService turnkeyService; // To be uncommented

    @Override
    @Transactional
    public EnrollWalletResponse enrollNewWallet(MautUser mautUser, String walletDisplayName) {
        if (mautUser == null) {
            log.error("MautUser cannot be null for wallet enrollment.");
            throw new IllegalArgumentException("Authenticated MautUser is required for wallet enrollment.");
        }
        log.info("Attempting to enroll new wallet for MautUser ID: {}", mautUser.getId());

        // 1. Check if user already has a wallet
        if (userWalletRepository.existsByMautUser(mautUser)) {
            log.warn("User ID: {} already has a wallet. Enrollment aborted.", mautUser.getId());
            throw new UserAlreadyHasWalletException("User already has an enrolled wallet.");
        }

        // --- Placeholder for Turnkey Integration --- //
        // String turnkeySubOrgId = "";
        // String turnkeyPrivateKeyId = "";
        // String newWalletAddress = "";
        //
        // try {
        //     log.debug("Creating Turnkey sub-organization for user ID: {}", mautUser.getId());
        //     TurnkeySubOrganization subOrg = turnkeyService.createSubOrganization("wallet-for-" + mautUser.getId().toString());
        //     turnkeySubOrgId = subOrg.getId();
        //     log.info("Turnkey sub-organization created with ID: {}", turnkeySubOrgId);
        //
        //     log.debug("Creating Turnkey private key for sub-organization ID: {}", turnkeySubOrgId);
        //     TurnkeyPrivateKey privateKey = turnkeyService.createPrivateKey(turnkeySubOrgId);
        //     turnkeyPrivateKeyId = privateKey.getId();
        //     newWalletAddress = privateKey.getAddress(); // Assuming TurnkeyPrivateKey has a getAddress() method
        //     log.info("Turnkey private key created with ID: {} and address: {}", turnkeyPrivateKeyId, newWalletAddress);
        //
        // } catch (Exception e) {
        //     log.error("Error during Turnkey operations for user ID: {}: {}", mautUser.getId(), e.getMessage(), e);
        //     throw new TurnkeyOperationException("Failed to create Turnkey resources: " + e.getMessage(), e);
        // }
        // --- End Placeholder --- //

        // For now, using placeholder values until TurnkeyService is implemented
        String turnkeySubOrgId = "placeholder-suborg-" + UUID.randomUUID().toString();
        String turnkeyPrivateKeyId = "placeholder-privkey-" + UUID.randomUUID().toString();
        String newWalletAddress = "0x" + UUID.randomUUID().toString().replace("-", "");
        log.info("Using placeholder Turnkey IDs. SubOrg: {}, PrivateKey: {}, WalletAddress: {}",
            turnkeySubOrgId, turnkeyPrivateKeyId, newWalletAddress);


        UserWallet newUserWallet = new UserWallet();
        newUserWallet.setMautUser(mautUser);
        newUserWallet.setWalletAddress(newWalletAddress);
        newUserWallet.setWalletDisplayName(walletDisplayName);
        newUserWallet.setTurnkeySubOrganizationId(turnkeySubOrgId); // Assuming UserWallet has these fields
        newUserWallet.setTurnkeyMautPrivateKeyId(turnkeyPrivateKeyId); // Corrected field name

        userWalletRepository.save(newUserWallet);
        log.info("New UserWallet record saved with ID: {} for MautUser ID: {}", newUserWallet.getId(), mautUser.getId());

        return new EnrollWalletResponse(newWalletAddress);
    }

    @Override
    public WalletDetailsResponse getWalletDetails(MautUser mautUser) {
        if (mautUser == null) {
            log.error("MautUser cannot be null for fetching wallet details.");
            // In a real app with Spring Security, this might be handled differently,
            // as @AuthenticationPrincipal would typically ensure mautUser is not null or deny access.
            throw new IllegalArgumentException("Authenticated MautUser is required to fetch wallet details.");
        }
        log.info("Fetching wallet details for MautUser ID: {}", mautUser.getId());

        UserWallet userWallet = userWalletRepository.findByMautUser(mautUser)
            .stream().findFirst()
            .orElseThrow(() -> { 
                log.warn("No UserWallet found for MautUser ID: {}. Cannot fetch details.", mautUser.getId());
                return new ResourceNotFoundException("User wallet not found.");
            }); 

        // Placeholder for current policy - this would involve another service call or lookup
        Map<String, Object> currentPolicyPlaceholder = new HashMap<>();
        currentPolicyPlaceholder.put("policyName", "No policy information available (Placeholder)");
        // currentPolicyPlaceholder.put("details", "Fetch from Turnkey or local policy store");

        WalletDetailsResponse response = WalletDetailsResponse.builder()
            .walletId(userWallet.getId().toString())
            .displayName(userWallet.getWalletDisplayName()) 
            .walletAddress(userWallet.getWalletAddress())
            .turnkeySubOrganizationId(userWallet.getTurnkeySubOrganizationId())
            .turnkeyMautPrivateKeyId(userWallet.getTurnkeyMautPrivateKeyId()) 
            .turnkeyUserPrivateKeyId(userWallet.getTurnkeyUserPrivateKeyId()) 
            .currentPolicy(currentPolicyPlaceholder)
            .build();

        log.debug("Returning wallet details for MautUser ID: {}: {}", mautUser.getId(), response);
        return response;
    }
}
