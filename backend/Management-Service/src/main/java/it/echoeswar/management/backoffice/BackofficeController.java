package it.echoeswar.management.backoffice;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.echoeswar.management.backoffice.payload.request.PackRequest;
import it.echoeswar.management.backoffice.payload.response.PackResponse;
import it.echoeswar.management.catalog.payload.request.CardRequest;
import it.echoeswar.management.catalog.payload.response.CardResponse;
import it.echoeswar.management.common.api.ResponseApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api")
@RequiredArgsConstructor
@Tag(
        name = "Backoffice",
        description = "Operations for managing backoffice"
)
public class BackofficeController {

    private final BackofficeService backofficeService;

    //Cards

    @Operation(
            summary = "Create a card",
            description = "Creats a new card. Faction, school and tier are referenced by identifier"
                    +"and must already exist. Restricted to authorized users."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Card created succesfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request payload",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
            @ApiResponse(responseCode = "403", description = "Not authorized to create cards", content = @Content),
            @ApiResponse(
                    responseCode = "404",
                    description = "Faction, school or tier not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("v1/private/cards")
    public ResponseEntity<?> createCard(@Valid @RequestBody CardRequest request){
        CardResponse card = backofficeService.createCard(request);
        return ResponseApi.buildResponse(HttpStatus.CREATED, card, "Card created succesfully");
    }

    //Packages

    @Operation(
            summary = "Create a pack",
            description = "Creates a new pack. The pack code must be unique (case-insensitive). "
                    + "Names are required, descriptions are optional. Restricted to authorized users."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Pack created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request payload",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
            @ApiResponse(responseCode = "403", description = "Not authorized to create packs", content = @Content),
            @ApiResponse(
                    responseCode = "409",
                    description = "A pack with the same code already exists",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("v1/private/packages")
    public ResponseEntity<?> createPack(@Valid @RequestBody PackRequest request){
        PackResponse pack = backofficeService.createPack(request);
        return ResponseApi.buildResponse(HttpStatus.CREATED, pack, "Pack created succesfully");
    }
}
