package it.echoeswar.management.pack.controller;

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
import it.echoeswar.management.pack.payload.response.GamePackageResponse;
import it.echoeswar.management.pack.payload.response.PackageOpeningResponse;
import it.echoeswar.management.pack.payload.response.PackagePurchaseResponse;
import it.echoeswar.management.pack.payload.response.UnopenedPackagePurchaseResponse;
import it.echoeswar.management.pack.service.GamePackageService;
import it.echoeswar.management.pack.service.PackagePurchaseService;
import it.echoeswar.management.security.userdetails.CustomUserDetails;
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
 * Exposes the authenticated game-package catalogue.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
@Tag(
        name = "Packs",
        description = "Operations for browsing, purchasing, and viewing game packages"
)
public class PackController {

    private final GamePackageService gamePackageService;
    private final PackagePurchaseService packagePurchaseService;

    @Operation(
            summary = "List available game packages",
            description = "Returns all active game packages available to authenticated users."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Available packages retrieved successfully",
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
                    description = "Authenticated principal is not allowed to access available packages",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            )
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/v1/private/packs")
    public ResponseEntity<ResponseApi<List<GamePackageResponse>>> getAvailablePackages() {
        List<GamePackageResponse> packages = gamePackageService.getAllAvailablePackages();

        if (packages.isEmpty()) {
            return ResponseApi.buildResponse(
                    HttpStatus.OK,
                    packages,
                    "No available packages found."
            );
        }

        return ResponseApi.buildResponse(HttpStatus.OK, packages);
    }

    @Operation(
            summary = "List the current user's unopened package purchases",
            description = "Returns package purchases owned by the authenticated user for which no opening has "
                    + "been recorded. Purchases are ordered from most recently purchased to oldest."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Unopened package purchases retrieved successfully",
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
                    description = "Only users can access their unopened package purchases",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            )
    })
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/v1/private/packs/purchases/unopened")
    public ResponseEntity<?> getUnopenedPurchases(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        List<UnopenedPackagePurchaseResponse> purchases = packagePurchaseService
                .getUnopenedPurchases(customUserDetails.getId());

        if (purchases.isEmpty()) {
            return ResponseApi.buildResponse(
                    HttpStatus.OK,
                    purchases,
                    "No unopened package purchases found."
            );
        }

        return ResponseApi.buildResponse(HttpStatus.OK, purchases);
    }

    @Operation(
            summary = "Open a purchased package",
            description = "Opens one package owned by the authenticated user and adds the five drawn cards to "
                    + "the user's collection. The first nine openings in each cycle grant three common cards, "
                    + "one rare card, and a random slot (85% common, 13% rare, 2% legendary). The tenth opening "
                    + "replaces that random slot with a guaranteed legendary card."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Package opened and cards added to the collection",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid purchase identifier",
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
                    description = "Only users can open their purchased packages",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The requested package purchase does not belong to the authenticated user",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "The package is already open or the card catalogue cannot satisfy the opening rules",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            )
    })
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/v1/private/packs/purchases/{purchaseId}/open")
    public ResponseEntity<?> openPackage(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Parameter(
                    in = ParameterIn.PATH,
                    description = "Identifier of the purchased package to open.",
                    example = "42"
            )
            @PathVariable @Positive Long purchaseId
    ) {
        PackageOpeningResponse opening = packagePurchaseService.openPackage(
                customUserDetails.getId(),
                purchaseId
        );

        return ResponseApi.buildResponse(
                HttpStatus.CREATED,
                opening,
                "Package opened successfully."
        );
    }

    @Operation(
            summary = "Purchase a game package",
            description = "Purchases the most recent active version of the selected package for the "
                    + "authenticated user. The operation atomically debits the wallet, records a "
                    + "PACKAGE_PURCHASE transaction, and creates an unopened package purchase."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Package purchased successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid package identifier",
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
                    description = "Only users can purchase packages",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Package is unavailable or the authenticated user has no wallet",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Wallet balance is insufficient to purchase the package",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            )
    })
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/v1/private/packs/{packageId}/purchases")
    public ResponseEntity<ResponseApi<PackagePurchaseResponse>> purchasePackage(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Parameter(
                    in = ParameterIn.PATH,
                    description = "Identifier of the catalog package to purchase.",
                    example = "1"
            )
            @PathVariable @Positive Long packageId
    ) {
        PackagePurchaseResponse purchase = packagePurchaseService.purchasePackage(
                customUserDetails.getId(),
                packageId
        );

        return ResponseApi.buildResponse(
                HttpStatus.CREATED,
                purchase,
                "Package purchased successfully."
        );
    }
}
