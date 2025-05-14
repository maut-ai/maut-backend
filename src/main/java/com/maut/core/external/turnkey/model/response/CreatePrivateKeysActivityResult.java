package com.maut.core.external.turnkey.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // For Jackson deserialization
public class CreatePrivateKeysActivityResult {
    // This field name should match the actual JSON key returned by the Turnkey API
    // for the V2 private key creation activity result.
    private CreatePrivateKeysResultV2Payload createPrivateKeysResultV2;
}
