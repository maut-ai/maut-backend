package com.maut.core.modules.activity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityStatusResponse {

    private String activityId;
    private String status;
    private String activityType;
    private Map<String, Object> result; // Optional: details of the activity result if completed

}
