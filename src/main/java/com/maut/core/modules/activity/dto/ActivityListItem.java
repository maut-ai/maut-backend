package com.maut.core.modules.activity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityListItem {

    private String activityId;
    private String status;
    private String activityType;
    private Instant createdAt; // When the activity was initiated
    private Instant updatedAt; // Last update timestamp for the activity
    // Potentially a summary of the result or a boolean indicating if it has a detailed result
    private String resultSummary; 

}
