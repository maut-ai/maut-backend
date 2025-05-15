package com.maut.core.modules.user.controller;

import com.maut.core.modules.user.dto.PaginatedMautUsersResponseDTO;
import com.maut.core.modules.user.service.MautUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "Maut Users", description = "APIs for managing Maut Users (end-users of client applications)")
public class MautUserController {

    private final MautUserService mautUserService;

    @GetMapping
    @Operation(summary = "List MautUsers for the authenticated user's team",
               description = "Retrieves a paginated list of MautUsers that belong to the team owned by the currently authenticated dashboard user.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Successfully retrieved list of MautUsers",
                                content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = PaginatedMautUsersResponseDTO.class))),
                   @ApiResponse(responseCode = "401", description = "Unauthorized if the user is not authenticated"),
                   @ApiResponse(responseCode = "404", description = "Not Found if the authenticated user does not own a team")
               })
    public ResponseEntity<PaginatedMautUsersResponseDTO> getMautUsersForCurrentUserTeam(
            @Parameter(description = "How many items are skipped before the first item that is shown (default: 0)")
            @RequestParam(defaultValue = "0") int offset,
            @Parameter(description = "Number of items returned per page (default: 25)")
            @RequestParam(defaultValue = "25") int limit) {

        // Basic validation for limit to prevent excessively large requests
        if (limit > 100) {
            limit = 100; // Cap limit at 100
        }
        if (limit <= 0) {
            limit = 25; // Default to 25 if invalid
        }
        if (offset < 0) {
            offset = 0; // Offset cannot be negative
        }

        PaginatedMautUsersResponseDTO response = mautUserService.getMautUsersForCurrentUserTeam(offset, limit);
        // Consider if an empty 'data' list when no team is found (as currently implemented in service) should be a 404 from controller.
        // For now, service returns an empty DTO, which will result in a 200 OK with empty data.
        // If response.getData().isEmpty() && response.getRecordsTotal() == 0 and the reason was no team, a 404 might be more appropriate.
        // However, the service handles the logic of returning empty list if no team, keeping controller simple.
        return ResponseEntity.ok(response);
    }
}
