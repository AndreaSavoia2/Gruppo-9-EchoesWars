package it.echoeswar.management.backoffice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.echoeswar.management.backoffice.payload.request.PackUpdateVersionRequest;
import it.echoeswar.management.backoffice.payload.response.FactionResponse;
import it.echoeswar.management.backoffice.payload.response.SchoolResponse;
import it.echoeswar.management.backoffice.payload.response.TierResponse;
import it.echoeswar.management.backoffice.service.BackofficeService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        return ResponseApi.buildResponse(HttpStatus.CREATED, pack, "Pack created successfully");
    }

    @Operation(
            summary = "Update the pull rules of a pack",
            description = "Creates a new version of an existing pack with updated pull rules and price. "
                    + "The pack is identified by its id. The new pull rule is validated before being saved. "
                    + "Restricted to authorized users."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pack pull rule updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request payload or invalid pull rule configuration",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
            @ApiResponse(responseCode = "403", description = "Not authorized to update packs", content = @Content),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pack not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("v1/private/packages/pull-rules")
    public ResponseEntity<?> updatePullRulePack(@Valid @RequestBody PackUpdateVersionRequest request){
        String result = backofficeService.updatePullRulePack(request);
        return ResponseApi.buildResponse(HttpStatus.OK, result, "Pack pull rule updated successfully");
    }

    @Operation(
            summary = "Get all tiers",
            description = "Returns the full list of tiers. Restricted to authorized users."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tiers retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
            @ApiResponse(responseCode = "403", description = "Not authorized to view tiers", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("v1/private/tiers")
    public ResponseEntity<?> getAllTiers(){
        List<TierResponse> tiers = backofficeService.getAllTiers();
        return ResponseApi.buildResponse(HttpStatus.OK, tiers, "Tiers retrieved successfully");
    }

    @Operation(
            summary = "Get all factions",
            description = "Returns the full list of factions. Restricted to authorized users."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Factions retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
            @ApiResponse(responseCode = "403", description = "Not authorized to view factions", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("v1/private/factions")
    public ResponseEntity<?> getAllFactions(){
        List<FactionResponse> factions = backofficeService.getAllFactions();
        return ResponseApi.buildResponse(HttpStatus.OK, factions, "Factions retrieved successfully");
    }

    @Operation(
            summary = "Get all schools",
            description = "Returns the full list of schools. Restricted to authorized users."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Schools retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
            @ApiResponse(responseCode = "403", description = "Not authorized to view schools", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("v1/private/schools")
    public ResponseEntity<?> getAllSchools(){
        List<SchoolResponse> schools = backofficeService.getAllSchools();
        return ResponseApi.buildResponse(HttpStatus.OK, schools, "Schools retrieved successfully");
    }



}
