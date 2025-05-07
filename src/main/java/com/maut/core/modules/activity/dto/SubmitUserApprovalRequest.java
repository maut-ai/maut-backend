package com.maut.core.modules.activity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitUserApprovalRequest {

    // For simplicity, using String. This might be a more complex object in a real scenario,
    // e.g., containing the signature and the original challenge or message.
    @NotBlank(message = "Signed challenge cannot be blank.")
    private String signedChallenge;

}
