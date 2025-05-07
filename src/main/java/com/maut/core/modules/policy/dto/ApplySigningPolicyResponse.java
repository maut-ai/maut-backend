package com.maut.core.modules.policy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplySigningPolicyResponse {

    private String status;
    private String turnkeyPolicyId;

}
