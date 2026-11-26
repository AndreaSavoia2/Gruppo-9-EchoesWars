package it.echoeswar.management.social.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.echoeswar.management.common.api.ResponseApi;
import it.echoeswar.management.security.userdetails.CustomUserDetails;
import it.echoeswar.management.social.payload.response.FriendshipRequestResponse;
import it.echoeswar.management.social.service.FriendshipService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Exposes friendship-request operations available to authenticated users.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
@Tag(
        name = "Friendships",
        description = "Operations for sending and managing friendship requests."
)
public class FriendshipController {

    private final FriendshipService friendshipService;

    /**
     * Sends a friendship request from the authenticated user to the specified recipient.
     *
     * @param customUserDetails authenticated requester details
     * @param recipientUsername username of the request recipient
     * @return a created friendship-request response
     */
    @Operation(
            summary = "Send a friendship request",
            description = "Creates a request for the specified user. The requester is derived exclusively "
                    + "from the authenticated user's token, and the request starts in the PENDING status."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Friendship request created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid username or a request sent to the same user",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bearer token is missing, invalid, expired, or revoked",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Recipient user does not exist or is no longer active",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "A friendship relationship already exists for this pair of users",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Only users can send friendship requests",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            )
    })
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/v1/private/friendships/requests/{recipientUsername}")
    public ResponseEntity<?> sendFriendRequest(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Parameter(
                    in = ParameterIn.PATH,
                    description = "Username of the user who will receive the request.",
                    example = "username",
                    required = true
            )
            @PathVariable @NotNull @NotBlank String recipientUsername
    ) {
        // The authenticated JWT principal is the only source of the requester identity.
        FriendshipRequestResponse friendshipRequest = friendshipService.sendFriendRequest(
                customUserDetails,
                recipientUsername
        );

        return ResponseApi.buildResponse(
                HttpStatus.CREATED,
                friendshipRequest,
                "Friendship request sent successfully."
        );
    }

    /**
     * Returns the pending friendship requests addressed to the authenticated user.
     *
     * @param customUserDetails authenticated recipient details
     * @return the pending received requests, possibly empty
     */
    @Operation(
            summary = "List received friendship requests",
            description = "Returns the pending friendship requests sent to the authenticated user, ordered "
                    + "from most recent to oldest."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Received friendship requests returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bearer token is missing, invalid, expired, or revoked",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Only users can view their received friendship requests",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            )
    })
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/v1/private/friendships/requests/received")
    public ResponseEntity<?> getReceivedFriendshipRequests(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        List<FriendshipRequestResponse> friendshipRequests = friendshipService
                .getReceivedFriendshipRequests(customUserDetails);

        return friendshipRequests.isEmpty()
                ? ResponseApi.buildResponse(
                        HttpStatus.OK,
                        friendshipRequests,
                        "No received friendship requests found."
                )
                : ResponseApi.buildResponse(HttpStatus.OK, friendshipRequests);
    }

    /**
     * Accepts a pending friendship request as its authenticated recipient.
     *
     * @param customUserDetails authenticated recipient details
     * @param friendshipId friendship-request identifier
     * @return the accepted friendship-request response
     */
    @Operation(
            summary = "Accept a friendship request",
            description = "Accepts a pending friendship request. Only the authenticated recipient can accept it."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Friendship request accepted successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid friendship request identifier",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bearer token is missing, invalid, expired, or revoked",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Only the request recipient can accept it",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Friendship request not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Friendship request is no longer pending",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            )
    })
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/v1/private/friendships/requests/{friendshipId}/accept")
    public ResponseEntity<?> acceptFriendRequest(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Parameter(
                    in = ParameterIn.PATH,
                    description = "Identifier of the friendship request to accept.",
                    example = "12",
                    required = true
            )
            @PathVariable @NotNull @Positive Long friendshipId
    ) {
        FriendshipRequestResponse friendshipRequest = friendshipService.acceptFriendRequest(
                customUserDetails,
                friendshipId
        );
        return ResponseApi.buildResponse(
                HttpStatus.OK,
                friendshipRequest,
                "Friendship request accepted successfully."
        );
    }

    /**
     * Rejects a pending friendship request as its authenticated recipient.
     *
     * @param customUserDetails authenticated recipient details
     * @param friendshipId friendship-request identifier
     * @return the rejected friendship-request response
     */
    @Operation(
            summary = "Reject a friendship request",
            description = "Rejects a pending friendship request. Only the authenticated recipient can reject it."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Friendship request rejected successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid friendship request identifier",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bearer token is missing, invalid, expired, or revoked",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Only the request recipient can reject it",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Friendship request not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Friendship request is no longer pending",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            )
    })
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/v1/private/friendships/requests/{friendshipId}/reject")
    public ResponseEntity<?> rejectFriendRequest(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Parameter(
                    in = ParameterIn.PATH,
                    description = "Identifier of the friendship request to reject.",
                    example = "12",
                    required = true
            )
            @PathVariable @NotNull @Positive Long friendshipId
    ) {
        FriendshipRequestResponse friendshipRequest = friendshipService.rejectFriendRequest(
                customUserDetails,
                friendshipId
        );
        return ResponseApi.buildResponse(
                HttpStatus.OK,
                friendshipRequest,
                "Friendship request rejected successfully."
        );
    }

    /**
     * Cancels a pending friendship request sent by the authenticated user.
     *
     * @param customUserDetails authenticated requester details
     * @param friendshipId friendship-request identifier
     * @return the cancelled friendship-request response
     */
    @Operation(
            summary = "Cancel a sent friendship request",
            description = "Cancels a pending friendship request. Only the authenticated requester can cancel it."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Friendship request cancelled successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid friendship request identifier",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bearer token is missing, invalid, expired, or revoked",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Only the request sender can cancel it",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Friendship request not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Friendship request is no longer pending",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            )
    })
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/v1/private/friendships/requests/{friendshipId}/cancel")
    public ResponseEntity<?> cancelSentFriendRequest(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Parameter(
                    in = ParameterIn.PATH,
                    description = "Identifier of the friendship request to cancel.",
                    example = "12",
                    required = true
            )
            @PathVariable @NotNull @Positive Long friendshipId
    ) {
        FriendshipRequestResponse friendshipRequest = friendshipService.cancelSentFriendRequest(
                customUserDetails,
                friendshipId
        );
        return ResponseApi.buildResponse(
                HttpStatus.OK,
                friendshipRequest,
                "Friendship request cancelled successfully."
        );
    }
}
