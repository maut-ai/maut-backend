package com.maut.core.modules.activity.controller;

import com.maut.core.modules.activity.dto.ActivityStatusResponse;
import com.maut.core.modules.activity.dto.ListActivitiesResponse;
import com.maut.core.modules.activity.dto.SubmitUserApprovalRequest;
import com.maut.core.modules.activity.dto.SubmitUserApprovalResponse;
import com.maut.core.modules.activity.service.ActivityService;
import com.maut.core.modules.user.model.MautUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.core.annotation.AuthenticationPrincipal; // For Spring Security
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping("/{activityId}/submit-user-approval")
    public ResponseEntity<SubmitUserApprovalResponse> submitUserApproval(
        // @AuthenticationPrincipal MautUser mautUser, // Placeholder
        @PathVariable String activityId,
        @Valid @RequestBody SubmitUserApprovalRequest request
    ) {
        // TODO: Replace null with actual authenticated MautUser from Spring Security context
        MautUser mautUser = null; // Placeholder

        SubmitUserApprovalResponse response = activityService.submitUserApproval(mautUser, activityId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{activityId}/status")
    public ResponseEntity<ActivityStatusResponse> getActivityStatus(
        // @AuthenticationPrincipal MautUser mautUser, // Placeholder, may not be needed if activityId is globally unique and public
        @PathVariable String activityId
    ) {
        // TODO: Replace null with actual authenticated MautUser if scoping by user is required
        MautUser mautUser = null; // Placeholder, depending on security model

        ActivityStatusResponse response = activityService.getActivityStatus(mautUser, activityId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("") // Maps to /v1/activities
    public ResponseEntity<ListActivitiesResponse> listActivities(
        // @AuthenticationPrincipal MautUser mautUser, // Placeholder, crucial for fetching user-specific activities
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam(defaultValue = "0") int offset,
        @RequestParam(required = false) String status // Optional filter by status
    ) {
        // TODO: Replace null with actual authenticated MautUser from Spring Security context
        MautUser mautUser = null; // Placeholder
        if (mautUser == null) {
            // This endpoint likely requires an authenticated user.
            // Handle appropriately, e.g., throw AuthenticationCredentialsNotFoundException
            // For now, this will be caught by the service layer if it requires a non-null user.
        }

        ListActivitiesResponse response = activityService.listActivities(mautUser, limit, offset, status);
        return ResponseEntity.ok(response);
    }
}
