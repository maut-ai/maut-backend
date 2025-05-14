package com.maut.core.external.turnkey.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubOrganizationRequest {
    @JsonProperty("type")
    private String type;

    @JsonProperty("timestampMs")
    private String timestampMs;

    @JsonProperty("organizationId")
    private String organizationId;

    @JsonProperty("parameters")
    private SubOrganizationParameters parameters;
}
