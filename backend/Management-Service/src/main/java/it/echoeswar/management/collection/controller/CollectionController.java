package it.echoeswar.management.collection.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.echoeswar.management.collection.service.UserCardService;
import it.echoeswar.management.common.api.ResponseApi;
import it.echoeswar.management.security.userdetails.CustomUserDetails;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
@Tag(
        name = "Collections",
        description = "Operations for browsing the authenticated user's card collection"
)
public class CollectionController {

    private final UserCardService userCardService;

    @Operation(
            summary = "Retrieve the current user's card collection",
            description = "Returns a zero-based page of cards owned by the authenticated user. "
                    + "The response data contains content, totalPages, totalElements, and currentPage. "
                    + "An empty page is returned with HTTP 200 and an explanatory message."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Collection page retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid pagination or sorting parameter",
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
                    description = "Authenticated principal is not allowed to access the collection",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Authenticated user no longer exists",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            )
    })
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/v1/private/collections")
    public ResponseEntity<ResponseApi<Map<String, Object>>> getUserCollection(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "Zero-based index of the page to retrieve.",
                    example = "0"
            )
            @RequestParam(defaultValue = "0") int pageNumber,
            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "Number of cards to include in each page.",
                    example = "6"
            )
            @RequestParam(defaultValue = "6") @Min(1) @Min(10) int pageSize,
            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "Collection-card property used to sort the result. Nested card properties are supported.",
                    example = "card.nameIt"
            )
            @RequestParam(defaultValue = "card.id") String sortBy,
            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "Sorting direction. Accepted values are ASC and DESC.",
                    example = "DESC"
            )
            @RequestParam(defaultValue = "DESC") String direction
            ){
        Map<String, Object> result = userCardService.getUserCollection(
                customUserDetails,
                pageNumber,
                pageSize,
                sortBy,
                direction);
        List<?> content = (List<?>) result.get("content");
        if (content.isEmpty())
            return ResponseApi.buildResponse(HttpStatus.OK, result, "No cards found for the requested page.");
        else
            return ResponseApi.buildResponse(HttpStatus.OK, result);
    }

}
