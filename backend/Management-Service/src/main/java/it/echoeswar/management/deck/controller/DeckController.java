package it.echoeswar.management.deck.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.echoeswar.management.common.api.ResponseApi;
import it.echoeswar.management.deck.enumerated.DeckStatusFilter;
import it.echoeswar.management.deck.payload.request.CreateDeckRequest;
import it.echoeswar.management.deck.payload.request.UpdateDeckRequest;
import it.echoeswar.management.deck.payload.response.DeckDetailResponse;
import it.echoeswar.management.deck.payload.response.DeckResponse;
import it.echoeswar.management.deck.payload.response.DeckSummaryResponse;
import it.echoeswar.management.deck.service.DeckService;
import it.echoeswar.management.security.userdetails.CustomUserDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Exposes deck operations for authenticated players. */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
@Tag(
        name = "Decks",
        description = "Operations for creating and managing the authenticated user's decks"
)
public class DeckController {

    private final DeckService deckService;

    @Operation(
            summary = "List decks for the current user",
            description = "Returns deck summaries without associated cards. The optional status filter defaults "
                    + "to READY; ALL includes both READY and ARCHIVED decks. DISABLED decks are never returned."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Decks retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "The status filter is invalid",
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
                    description = "Only users can list their decks",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            )
    })
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/v1/private/decks")
    public ResponseEntity<?> getUserDecks(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Parameter(
                    description = "Deck statuses to include. READY is the default; ALL includes READY and ARCHIVED.",
                    example = "ALL"
            )
            @RequestParam(name = "status", defaultValue = "READY") DeckStatusFilter status
    ) {
        List<DeckSummaryResponse> decks = deckService.getUserDecks(customUserDetails, status);

        return decks.isEmpty() ?
                ResponseApi.buildResponse(HttpStatus.OK, decks, "No decks found for this user") :
                ResponseApi.buildResponse(HttpStatus.OK, decks);
    }

    @Operation(
            summary = "Get a deck detail for the current user",
            description = "Returns one visible deck owned by the authenticated user, including each distinct "
                    + "catalog card once. The quantity on every card is the number of copies in that deck."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Deck detail retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "The deck identifier is invalid",
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
                    description = "Only users can view their deck details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The deck does not exist or is not visible to the current user",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            )
    })
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/v1/private/decks/{deckId}")
    public ResponseEntity<?> getUserDeckDetail(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Parameter(description = "Identifier of a deck owned by the current user.", example = "42")
            @PathVariable @Positive Long deckId
    ) {
        DeckDetailResponse deck = deckService.getUserDeckDetail(customUserDetails, deckId);

        return ResponseApi.buildResponse(HttpStatus.OK, deck);
    }

    @Operation(
            summary = "Create a deck for the current user",
            description = "Creates a complete ready-to-play deck. Card quantities are validated against "
                    + "the selected active deck rule, the two selected factions, and the user's collection; "
                    + "copies are not consumed and can also be used in other decks."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Deck created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "The request or deck composition violates a deck rule",
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
                    description = "Only users can create decks",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The authenticated user, deck rule, or selected faction does not exist",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Deck limit, ownership, availability, or name constraint conflicts with the request",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            )
    })
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/v1/private/decks")
    public ResponseEntity<?> createDeck(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @NotNull @RequestBody CreateDeckRequest request
    ) {
        DeckResponse deck = deckService.createDeck(customUserDetails, request);

        return ResponseApi.buildResponse(
                HttpStatus.CREATED,
                deck,
                "Deck created successfully."
        );
    }

    @Operation(
            summary = "Update a deck for the current user",
            description = "Replaces the deck name and complete card composition. The deck's "
                    + "existing factions remain unchanged, and the new composition must satisfy the rule "
                    + "stored with the deck and the user's current collection."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Deck updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "The request or replacement composition violates a deck rule",
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
                    description = "Only users can update their decks",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The deck does not exist or is not visible to the current user",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "The name, card ownership, or card availability conflicts with the request",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            )
    })
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/v1/private/decks/{deckId}")
    public ResponseEntity<?> updateDeck(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Parameter(description = "Identifier of a deck owned by the current user.", example = "42")
            @PathVariable @Positive Long deckId,
            @Valid @NotNull @RequestBody UpdateDeckRequest request
    ) {
        // The controller passes only the identity extracted from the token to the service, never a user supplied by the client.
        DeckResponse deck = deckService.updateDeck(customUserDetails, deckId, request);

        return ResponseApi.buildResponse(
                HttpStatus.OK,
                deck,
                "Deck updated successfully."
        );
    }

    @Operation(
            summary = "Delete a deck for the current user",
            description = "Soft-deletes a visible deck owned by the authenticated user. The deck is no longer "
                    + "returned and no longer counts toward the user's active deck limit."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Deck deleted successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "The deck identifier is invalid",
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
                    description = "Only users can delete their decks",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The deck does not exist or is not visible to the current user",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            )
    })
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/v1/private/decks/{deckId}")
    public ResponseEntity<?> deleteDeck(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Parameter(description = "Identifier of a deck owned by the current user.", example = "42")
            @PathVariable @Positive Long deckId
    ) {
        deckService.deleteDeck(customUserDetails, deckId);

        return ResponseApi.buildResponse(HttpStatus.OK, "Deck deleted successfully.");
    }
}
