package com.maut.core.external.turnkey.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActivityResponsePayload<T> {
    @JsonProperty("id")
    private String id;

    @JsonProperty("status")
    private String status;

    @JsonProperty("type")
    private String type;

    @JsonProperty("organizationId")
    private String organizationId;

    @JsonProperty("timestampMs")
    private String timestampMs;

    @JsonProperty("result")
    private T result;

    @JsonProperty("error")
    private ActivityError error;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ActivityError {
        @JsonProperty("errorCode")
        private String errorCode;
        @JsonProperty("errorMessage")
        private String errorMessage;
    }
}
