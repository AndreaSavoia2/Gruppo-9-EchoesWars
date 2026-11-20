package it.echoeswar.management.credit.controller;

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
import it.echoeswar.management.credit.payload.response.CreditBalanceResponse;
import it.echoeswar.management.credit.service.CreditTransactionService;
import it.echoeswar.management.security.userdetails.CustomUserDetails;
import jakarta.validation.constraints.Max;
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

/**
 * Exposes the authenticated user's credit balance and transaction history.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
@Tag(
        name = "Credits",
        description = "Operations for viewing the authenticated user's credit balance and transaction history"
)
public class CreditController {

    private final CreditTransactionService creditTransactionService;

    @Operation(
            summary = "List the current user's credit transactions",
            description = "Returns a zero-based page of credit transactions belonging to the authenticated user. "
                    + "The response data contains content, totalPages, totalElements, and currentPage. "
                    + "An empty page is returned with HTTP 200 and an explanatory message."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Credit transaction page retrieved successfully",
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
                    description = "Only users can access their credit transaction history",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            )
    })
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/v1/private/credits")
    public ResponseEntity<?> getAllTransactionsForUser(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "Zero-based index of the page to retrieve.",
                    example = "0"
            )
            @RequestParam(defaultValue = "0") @Min(0) int pageNumber,
            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "Number of credit transactions to include in each page. Allowed range: 1 to 10.",
                    example = "6"
            )
            @RequestParam(defaultValue = "6") @Min(1) @Max(10) int pageSize,
            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "Credit-transaction property used to sort the result.",
                    example = "createdAt"
            )
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "Sorting direction. Accepted values are ASC and DESC.",
                    example = "DESC"
            )
            @RequestParam(defaultValue = "DESC") String direction
    ){
        Map<String, Object> result = creditTransactionService.getAllTransactionForUser(
                customUserDetails,
                pageNumber,
                pageSize,
                sortBy,
                direction);
        List<?> content = (List<?>) result.get("content");
        return content.isEmpty() ?
                ResponseApi.buildResponse(HttpStatus.OK, result, "No transaction found for the requested page.") :
                ResponseApi.buildResponse(HttpStatus.OK, result);
    }

    @Operation(
            summary = "Retrieve the current credit balance",
            description = "Returns the current credit balance of the authenticated user's wallet."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Current credit balance retrieved successfully",
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
                    description = "Only users can access their current credit balance",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The authenticated user does not have a credit wallet",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            )
    })
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/v1/private/credits/balance")
    public ResponseEntity<?> getCurrentCreditBalance(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        CreditBalanceResponse balance = creditTransactionService
                .getCurrentCreditBalance(customUserDetails);

        return ResponseApi.buildResponse(
                HttpStatus.OK,
                balance,
                "Current credit balance retrieved successfully."
        );
    }

}
