package com.maut.core.modules.policy.service;

import com.maut.core.modules.policy.dto.ApplySigningPolicyRequest;
import com.maut.core.modules.policy.dto.ApplySigningPolicyResponse;
import com.maut.core.modules.user.model.MautUser;

public interface PolicyService {

    /**
     * Applies a new signing policy to the Turnkey sub-organization associated with the MautUser's wallet.
     *
     * @param mautUser The MautUser for whom the policy is being applied. Must not be null.
     * @param request The request containing the policy name and details.
     * @return ApplySigningPolicyResponse containing the status and Turnkey policy ID.
     * @throws com.maut.core.common.exception.TurnkeyOperationException if an error occurs during Turnkey operations.
     * @throws com.maut.core.common.exception.ResourceNotFoundException if the user's wallet (and associated Turnkey sub-org) is not found.
     * @throws com.maut.core.common.exception.InvalidRequestException if the policy details are invalid.
     */
    ApplySigningPolicyResponse applySigningPolicy(MautUser mautUser, ApplySigningPolicyRequest request);

}
