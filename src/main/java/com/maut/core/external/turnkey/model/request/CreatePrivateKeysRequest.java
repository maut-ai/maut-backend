package com.maut.core.external.turnkey.model.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreatePrivateKeysRequest {
    private String type; // e.g., "ACTIVITY_TYPE_CREATE_PRIVATE_KEYS_V2"
    private String timestampMs;
    private String organizationId;
    private CreatePrivateKeysParameters parameters;
}
