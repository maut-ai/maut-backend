package com.maut.core.modules.policy.controller;

import com.maut.core.modules.policy.dto.ApplySigningPolicyRequest;
import com.maut.core.modules.policy.dto.ApplySigningPolicyResponse;
import com.maut.core.modules.policy.service.PolicyService;
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
@RequestMapping("/v1/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @PostMapping("/apply-signing-policy")
    public ResponseEntity<ApplySigningPolicyResponse> applySigningPolicy(
        // @AuthenticationPrincipal MautUser mautUser, // Placeholder for authenticated MautUser
        @Valid @RequestBody ApplySigningPolicyRequest request
    ) {
        // TODO: Replace null with actual authenticated MautUser from Spring Security context
        MautUser mautUser = null; // Placeholder

        ApplySigningPolicyResponse response = policyService.applySigningPolicy(mautUser, request);
        return ResponseEntity.ok(response);
    }
}
