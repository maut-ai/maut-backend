package com.maut.core.modules.policy.service;

import com.maut.core.common.exception.InvalidRequestException;
import com.maut.core.common.exception.ResourceNotFoundException;
// import com.maut.core.common.exception.TurnkeyOperationException; // For actual Turnkey calls
import com.maut.core.modules.policy.dto.ApplySigningPolicyRequest;
import com.maut.core.modules.policy.dto.ApplySigningPolicyResponse;
import com.maut.core.modules.user.model.MautUser;
import com.maut.core.modules.wallet.model.UserWallet;
import com.maut.core.modules.wallet.repository.UserWalletRepository;
// import com.maut.core.external.turnkey.TurnkeyService; // To be created
// import com.maut.core.external.turnkey.model.TurnkeyPolicyResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyServiceImpl implements PolicyService {

    private final UserWalletRepository userWalletRepository;
    // private final TurnkeyService turnkeyService; // To be uncommented

    @Override
    public ApplySigningPolicyResponse applySigningPolicy(MautUser mautUser, ApplySigningPolicyRequest request) {
        if (mautUser == null) {
            log.error("MautUser cannot be null for applying signing policy.");
            throw new IllegalArgumentException("Authenticated MautUser is required for applying signing policy.");
        }
        if (request == null || request.getPolicyDetails() == null || request.getPolicyDetails().isEmpty()) {
            log.error("Request or policy details cannot be null/empty for MautUser ID: {}", mautUser.getId());
            throw new InvalidRequestException("Policy details are required.");
        }
        if (request.getPolicyName() == null || request.getPolicyName().isBlank()) {
            log.error("Policy name cannot be null/blank for MautUser ID: {}", mautUser.getId());
            throw new InvalidRequestException("Policy name is required.");
        }
        log.info("Applying signing policy '{}' for MautUser ID: {}", request.getPolicyName(), mautUser.getId());

        // 1. Find the user's wallet to get the Turnkey Sub-Organization ID
        UserWallet userWallet = userWalletRepository.findByMautUser(mautUser)
            .stream().findFirst()
            .orElseThrow(() -> {
                log.warn("No UserWallet found for MautUser ID: {}. Cannot apply policy.", mautUser.getId());
                return new ResourceNotFoundException("User wallet not found, cannot apply policy.");
            });

        String turnkeySubOrganizationId = userWallet.getTurnkeySubOrganizationId();
        if (turnkeySubOrganizationId == null || turnkeySubOrganizationId.isBlank()) {
            log.error("UserWallet ID: {} for MautUser ID: {} has no Turnkey Sub-Organization ID.", userWallet.getId(), mautUser.getId());
            throw new IllegalStateException("User wallet is missing Turnkey Sub-Organization ID.");
        }

        // --- Placeholder for Turnkey Integration --- //
        // String turnkeyPolicyId;
        // String status;
        // try {
        //     log.debug("Applying policy '{}' to Turnkey sub-organization ID: {}", request.getPolicyName(), turnkeySubOrganizationId);
        //     TurnkeyPolicyResult result = turnkeyService.applyPolicy(
        // turnkeySubOrganizationId,
        // request.getPolicyName(),
        // request.getPolicyDetails()
        //     );
        //     turnkeyPolicyId = result.getPolicyId();
        //     status = result.getStatus(); // e.g., "ACTIVE", "PENDING_APPROVAL"
        //     log.info("Policy '{}' applied with ID: {} and status: {} for MautUser ID: {}", 
        // request.getPolicyName(), turnkeyPolicyId, status, mautUser.getId());
        // } catch (TurnkeyOperationException e) {
        //     log.error("Turnkey operation failed while applying policy for MautUser ID: {}: {}", mautUser.getId(), e.getMessage(), e);
        //     throw e;
        // } catch (Exception e) {
        //     log.error("Error applying policy with Turnkey for MautUser ID: {}: {}", mautUser.getId(), e.getMessage(), e);
        //     throw new TurnkeyOperationException("Failed to apply policy with Turnkey: " + e.getMessage(), e);
        // }
        // --- End Placeholder --- //

        // For now, returning placeholder data
        log.warn("TurnkeyService not yet implemented. Simulating successful policy application for MautUser ID: {}", mautUser.getId());
        String placeholderPolicyId = "turnkey_policy_id_" + java.util.UUID.randomUUID().toString();
        String placeholderStatus = "SUCCESS"; // Or "PENDING_APPROVAL"

        // Optionally, update UserWallet or a new Policy entity if we need to store policy info locally
        // userWallet.setCurrentTurnkeyPolicyId(placeholderPolicyId);
        // userWalletRepository.save(userWallet);

        return new ApplySigningPolicyResponse(placeholderStatus, placeholderPolicyId);
    }
}
