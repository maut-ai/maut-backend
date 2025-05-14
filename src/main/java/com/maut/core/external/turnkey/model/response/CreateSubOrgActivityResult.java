package com.maut.core.external.turnkey.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateSubOrgActivityResult {
    @JsonProperty("createSubOrganizationResultV7")
    private SubOrganizationDetails createSubOrganizationResultV7;
}
