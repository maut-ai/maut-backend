package com.maut.core.modules.policy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplySigningPolicyRequest {

    @NotBlank(message = "Policy name cannot be blank.")
    @Size(max = 255, message = "Policy name cannot exceed 255 characters.")
    private String policyName;

    @NotNull(message = "Policy details cannot be null.")
    @NotEmpty(message = "Policy details cannot be empty.")
    private Map<String, Object> policyDetails; // Represents the Turnkey policy object structure

}
