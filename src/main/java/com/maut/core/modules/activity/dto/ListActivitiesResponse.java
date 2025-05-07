package com.maut.core.modules.activity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListActivitiesResponse {

    private List<ActivityListItem> activities;
    private int limit;
    private int offset;
    private long totalActivities; // Total number of activities matching the criteria (for pagination)

}
