package com.maut.core.modules.wallet.service;

import com.maut.core.common.exception.ResourceNotFoundException; 
import com.maut.core.common.exception.UserAlreadyHasWalletException;
import com.maut.core.modules.user.model.MautUser;
import com.maut.core.modules.wallet.dto.EnrollWalletResponse;
import com.maut.core.modules.wallet.dto.WalletDetailsResponse;
import com.maut.core.modules.wallet.model.UserWallet;
import com.maut.core.modules.wallet.repository.UserWalletRepository;
import com.maut.core.external.turnkey.TurnkeyService; 
import com.maut.core.external.turnkey.model.TurnkeyPrivateKey;
import com.maut.core.external.turnkey.model.TurnkeySubOrganization;
import com.maut.core.integration.turnkey.exception.TurnkeyOperationException; 

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final UserWalletRepository userWalletRepository;
    private final TurnkeyService turnkeyService; 

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

        String turnkeySubOrgId;
        String mautTurnkeyPrivateKeyId;
        String userTurnkeyPrivateKeyId;
        String newWalletAddress;
        // String defaultPolicyId = null; 

        try {
            log.debug("Creating Turnkey sub-organization for MautUser ID: {}", mautUser.getId());
            String subOrgName = "maut-wallet-" + mautUser.getId();
            TurnkeySubOrganization subOrg = turnkeyService.createSubOrganization(subOrgName);
            turnkeySubOrgId = subOrg.getSubOrganizationId();
            log.info("Turnkey sub-organization created with ID: {}", turnkeySubOrgId);

            log.debug("Creating Maut-managed Turnkey private key for sub-organization ID: {}", turnkeySubOrgId);
            String mautManagedKeyName = "maut-managed-key-" + turnkeySubOrgId;
            TurnkeyPrivateKey mautPrivateKey = turnkeyService.createMautManagedPrivateKey(turnkeySubOrgId, mautManagedKeyName);
            mautTurnkeyPrivateKeyId = mautPrivateKey.getPrivateKeyId();
            newWalletAddress = mautPrivateKey.getAddress(); 
            log.info("Maut-managed Turnkey private key created with ID: {} and address: {}", mautTurnkeyPrivateKeyId, newWalletAddress);

            log.debug("Creating User-controlled Turnkey private key for sub-organization ID: {}, MautUser ID: {}", turnkeySubOrgId, mautUser.getId().toString());
            String userControlledKeyName = "user-controlled-key-" + mautUser.getId();
            TurnkeyPrivateKey userPrivateKey = turnkeyService.createUserControlledPrivateKey(turnkeySubOrgId, mautUser.getId().toString(), userControlledKeyName);
            userTurnkeyPrivateKeyId = userPrivateKey.getPrivateKeyId();
            log.info("User-controlled Turnkey private key created with ID: {}", userTurnkeyPrivateKeyId);
            
            // Optional: Assign default policy
            // defaultPolicyId = turnkeyService.assignDefaultPolicyToPrivateKey(mautTurnkeyPrivateKeyId, "YOUR_DEFAULT_POLICY_ID_HERE");
            // log.info("Assigned default policy. Activity ID: {}", defaultPolicyId);

        } catch (TurnkeyOperationException e) { 
            log.error("Error during Turnkey operations for MautUser ID: {}: {}", mautUser.getId(), e.getMessage(), e);
            throw new TurnkeyOperationException("Failed to create Turnkey resources during wallet enrollment: " + e.getMessage(), e);
        } catch (Exception e) { 
            log.error("Unexpected error during Turnkey operations for MautUser ID: {}: {}", mautUser.getId(), e.getMessage(), e);
            throw new RuntimeException("Unexpected error during wallet enrollment: " + e.getMessage(), e);
        }

        UserWallet newUserWallet = new UserWallet();
        newUserWallet.setMautUser(mautUser);
        newUserWallet.setWalletAddress(newWalletAddress); 
        newUserWallet.setWalletDisplayName(walletDisplayName);
        newUserWallet.setTurnkeySubOrganizationId(turnkeySubOrgId);
        newUserWallet.setTurnkeyMautPrivateKeyId(mautTurnkeyPrivateKeyId); 
        newUserWallet.setTurnkeyUserPrivateKeyId(userTurnkeyPrivateKeyId); 
        // newUserWallet.setDefaultTurnkeyPolicyId(defaultPolicyId); 

        UserWallet savedWallet = userWalletRepository.save(newUserWallet);
        log.info("Successfully enrolled new wallet with ID: {} for MautUser ID: {}", savedWallet.getId(), mautUser.getId());

        return new EnrollWalletResponse(savedWallet.getId().toString(), savedWallet.getWalletAddress());
    }

    @Override
    public WalletDetailsResponse getWalletDetails(MautUser mautUser) {
        if (mautUser == null) {
            log.error("MautUser cannot be null for fetching wallet details.");
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
