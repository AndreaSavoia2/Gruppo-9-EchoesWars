package it.echoeswar.management.catalog.controller;

import it.echoeswar.management.catalog.service.CardService;
import it.echoeswar.management.common.api.ResponseApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("api")
@Tag(
        name = "Catalog",
        description = "Operations for browsing the public card catalogue"
)
public class CatalogController {

    private final CardService cardService;

    @Operation(
            summary = "List catalog cards",
            description = "Returns a zero-based page of cards ordered by the requested card property. "
                    + "The response data contains content, totalPages, totalElements, and currentPage."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Catalog page retrieved successfully",
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
            )
    })
    @GetMapping("v1/public/catalogs")
    public ResponseEntity<ResponseApi<Map<String, Object>>> getAllCards(
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
            @RequestParam(defaultValue = "6") int pageSize,
            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "Card property used to sort the result.",
                    example = "nameIt"
            )
            @RequestParam(defaultValue = "nameIt") String sortBy,
            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "Sorting direction. Accepted values are ASC and DESC.",
                    example = "DESC"
            )
            @RequestParam(defaultValue = "DESC") String direction) {
        Map<String, Object> result = cardService.getAllCards(pageNumber, pageSize, sortBy, direction);
        List<?> content = (List<?>) result.get("content");
        if (content.isEmpty())
            return ResponseApi.buildResponse(HttpStatus.OK, result, "No cards found for the requested page.");
        else
            return ResponseApi.buildResponse(HttpStatus.OK, result);
    }
}
