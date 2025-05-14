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
public class RootUserPayload {
    @JsonProperty("userName")
    private String userName;

    @JsonProperty("apiKeys")
    private List<Object> apiKeys; 

    @JsonProperty("authenticators")
    private List<Object> authenticators; 

    @JsonProperty("oauthProviders")
    private List<Object> oauthProviders; 
}
