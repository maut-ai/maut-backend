package com.maut.core.external.turnkey.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor // For Jackson deserialization
public class CreatePrivateKeysResultV2Payload {
    private List<PrivateKeyDetails> privateKeys;
}
