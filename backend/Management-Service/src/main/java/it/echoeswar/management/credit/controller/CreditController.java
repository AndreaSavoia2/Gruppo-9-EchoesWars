package it.echoeswar.management.credit.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import it.echoeswar.management.common.api.ResponseApi;
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

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class CreditController {

    private final CreditTransactionService creditTransactionService;

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/v1/private/credits")
    public ResponseEntity<?> getAllTransactionForUser(
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
            @RequestParam(defaultValue = "6") @Min(1) @Max(10) int pageSize,
            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "Collection-card property used to sort the result. Nested card properties are supported.",
                    example = "card.nameIt"
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
        if (content.isEmpty())
            return ResponseApi.buildResponse(HttpStatus.OK, result, "No transaction found for the requested page.");
        else
            return ResponseApi.buildResponse(HttpStatus.OK, result);
    }

}
