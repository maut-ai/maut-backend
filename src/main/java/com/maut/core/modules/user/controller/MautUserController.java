package com.maut.core.modules.user.controller;

import com.maut.core.modules.user.dto.PaginatedMautUsersResponseDTO;
import com.maut.core.modules.user.dto.AuthenticatorDetailResponseDto;
import com.maut.core.modules.user.dto.MautUserDetailResponseDto;
import com.maut.core.modules.user.dto.WalletDetailResponseDto;
import com.maut.core.modules.user.model.MautUser;
import com.maut.core.modules.user.model.User;
import com.maut.core.modules.user.service.MautUserService;
import com.maut.core.modules.authenticator.service.AuthenticatorService;
import com.maut.core.modules.wallet.dto.WalletDetailsResponse;
import com.maut.core.modules.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.maut.core.common.exception.ResourceNotFoundException;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "Maut Users", description = "APIs for managing Maut Users (end-users of client applications)")
public class MautUserController {

    private final MautUserService mautUserService;
    private final AuthenticatorService authenticatorService;
    private final WalletService walletService;

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

    @GetMapping("/{userId}")
    @Operation(summary = "Get MautUser details by ID",
               description = "Retrieves detailed information about a specific MautUser, including their wallets and authenticators. Access is restricted to dashboard users who own the team the MautUser belongs to.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Successfully retrieved MautUser details",
                                content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = MautUserDetailResponseDto.class))),
                   @ApiResponse(responseCode = "401", description = "Unauthorized if the dashboard user is not authenticated"),
                   @ApiResponse(responseCode = "403", description = "Forbidden if the authenticated dashboard user does not have access to this MautUser"),
                   @ApiResponse(responseCode = "404", description = "Not Found if the MautUser with the given ID does not exist")
               })
    public ResponseEntity<?> getMautUserDetail(
            @Parameter(description = "UUID of the MautUser to retrieve") @PathVariable UUID userId,
            @AuthenticationPrincipal User authenticatedUser) {

        if (authenticatedUser == null) {
            // This case should ideally be handled by Spring Security, but as a safeguard:
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }

        Optional<MautUser> mautUserOptional = mautUserService.findMautUserById(userId);
        if (mautUserOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("MautUser not found with ID: " + userId);
        }
        MautUser mautUser = mautUserOptional.get();

        if (!mautUserService.isMautUserAccessibleBy(mautUser, authenticatedUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied to MautUser with ID: " + userId);
        }

        List<AuthenticatorDetailResponseDto> authenticators = authenticatorService.listWebauthnCredentialsForMautUser(mautUser);

        List<WalletDetailResponseDto> wallets;
        try {
            WalletDetailsResponse walletDetailsResponse = walletService.getWalletDetails(mautUser);
            WalletDetailResponseDto walletDto = new WalletDetailResponseDto(
                    UUID.fromString(walletDetailsResponse.getWalletId()),
                    walletDetailsResponse.getCreatedAt() != null ? LocalDateTime.ofInstant(walletDetailsResponse.getCreatedAt(), ZoneOffset.UTC) : null,
                    walletDetailsResponse.getWalletAddress(),
                    walletDetailsResponse.getDisplayName()
            );
            wallets = Collections.singletonList(walletDto);
        } catch (ResourceNotFoundException e) {
            // If wallet not found, it's not an error for this endpoint, just means no wallet details to show.
            wallets = Collections.emptyList();
        } catch (Exception e) {
            // Log other unexpected errors from wallet service
            // Consider how to handle this - for now, treat as no wallet and proceed
            // This might also indicate a broader issue that should be logged with more severity
            System.err.println("Error fetching wallet details for MautUser ID " + mautUser.getId() + ": " + e.getMessage());
            wallets = Collections.emptyList(); 
        }

        MautUserDetailResponseDto responseDto = new MautUserDetailResponseDto(
                mautUser.getId(),
                mautUser.getMautUserId(),
                mautUser.getClientSystemUserId(),
                mautUser.getClientApplication() != null ? mautUser.getClientApplication().getMautApiClientId() : null,
                mautUser.getCreatedAt(), // Corrected: MautUser.createdAt is already LocalDateTime
                wallets,
                authenticators
        );

        return ResponseEntity.ok(responseDto);
    }
}
