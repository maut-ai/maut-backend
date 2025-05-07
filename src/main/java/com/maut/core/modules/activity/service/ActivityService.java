package com.maut.core.modules.activity.service;

import com.maut.core.modules.activity.dto.SubmitUserApprovalRequest;
import com.maut.core.modules.activity.dto.SubmitUserApprovalResponse;
import com.maut.core.modules.activity.dto.ActivityStatusResponse;
import com.maut.core.modules.activity.dto.ListActivitiesResponse;
import com.maut.core.modules.user.model.MautUser;

public interface ActivityService {

    /**
     * Submits a user's approval (e.g., signed challenge) for a pending Turnkey activity.
     *
     * @param mautUser The MautUser submitting the approval. Must not be null.
     * @param activityId The ID of the Turnkey activity being approved.
     * @param request The request containing the signed challenge or approval data.
     * @return SubmitUserApprovalResponse containing the status of the approval.
     * @throws com.maut.core.common.exception.TurnkeyOperationException if an error occurs during Turnkey operations.
     * @throws com.maut.core.common.exception.ResourceNotFoundException if the activity or associated resources are not found.
     * @throws com.maut.core.common.exception.InvalidRequestException if the approval data is invalid.
     */
    SubmitUserApprovalResponse submitUserApproval(MautUser mautUser, String activityId, SubmitUserApprovalRequest request);

    /**
     * Retrieves the current status of a Turnkey activity.
     *
     * @param mautUser The MautUser requesting the status. May be null if activity is public or not user-scoped.
     * @param activityId The ID of the Turnkey activity.
     * @return ActivityStatusResponse containing the activity's ID, status, type, and optional result.
     * @throws com.maut.core.common.exception.TurnkeyOperationException if an error occurs during Turnkey operations.
     * @throws com.maut.core.common.exception.ResourceNotFoundException if the activity is not found.
     */
    ActivityStatusResponse getActivityStatus(MautUser mautUser, String activityId);

    /**
     * Lists recent activities for the authenticated user, with pagination and optional status filtering.
     *
     * @param mautUser The MautUser for whom to list activities. Must not be null.
     * @param limit The maximum number of activities to return.
     * @param offset The number of activities to skip (for pagination).
     * @param status Optional filter for activity status.
     * @return ListActivitiesResponse containing the list of activities and pagination details.
     * @throws com.maut.core.common.exception.TurnkeyOperationException if an error occurs during Turnkey operations.
     * @throws IllegalArgumentException if mautUser is null.
     */
    ListActivitiesResponse listActivities(MautUser mautUser, int limit, int offset, String status);

}
