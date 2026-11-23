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
import java.util.UUID;

/**
 * Espone le operazioni sociali disponibili agli utenti autenticati.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
@Tag(
        name = "Friendships",
        description = "Operazioni per inviare e gestire le richieste di amicizia"
)
public class FriendshipController {

    private final FriendshipService friendshipService;

    @Operation(
            summary = "Invia una richiesta di amicizia",
            description = "Crea una richiesta verso l'utente indicato. Il mittente viene ricavato "
                    + "esclusivamente dal token dell'utente autenticato e la richiesta nasce in stato PENDING."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Richiesta di amicizia creata correttamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Identificativo non valido oppure richiesta inviata a se stessi",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token Bearer mancante, non valido, scaduto o revocato",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Utente destinatario inesistente o non più attivo",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Esiste già una relazione di amicizia per questa coppia di utenti",
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
                    description = "Identificativo uername dell'utente che riceverà la richiesta.",
                    example = "Username",
                    required = true
            )
            @PathVariable @NotNull @NotBlank String recipientUsername
    ) {
        /*
         * customUserDetails contiene il principal creato da Spring Security dal
         * JWT valido: non accettiamo quindi dal client alcun id del mittente.
         */
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

    @Operation(
            summary = "Elenca le richieste di amicizia ricevute",
            description = "Restituisce le richieste di amicizia ancora pendenti inviate all'utente autenticato, "
                    + "ordinate dalla più recente."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Richieste di amicizia ricevute correttamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token Bearer mancante, non valido, scaduto o revocato",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Solo gli utenti possono visualizzare le proprie richieste di amicizia",
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
}
