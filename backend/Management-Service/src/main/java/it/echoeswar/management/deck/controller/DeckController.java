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
import it.echoeswar.management.deck.payload.request.CreateDeckRequest;
import it.echoeswar.management.deck.payload.response.DeckResponse;
import it.echoeswar.management.deck.service.DeckService;
import it.echoeswar.management.security.userdetails.CustomUserDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<ResponseApi<DeckResponse>> createDeck(
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
}
