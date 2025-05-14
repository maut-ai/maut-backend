package com.maut.core.external.turnkey.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubOrganizationParameters {
    @JsonProperty("subOrganizationName")
    private String subOrganizationName;

    @JsonProperty("rootUsers")
    private List<RootUserPayload> rootUsers;

    @JsonProperty("rootQuorumThreshold")
    private int rootQuorumThreshold;
}
