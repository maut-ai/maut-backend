package com.maut.core.external.turnkey.model.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CreatePrivateKeysParameters {
    private String subOrganizationId;
    private List<PrivateKeySpecification> privateKeys;
}
