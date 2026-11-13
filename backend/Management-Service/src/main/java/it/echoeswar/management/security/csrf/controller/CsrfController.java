package it.echoeswar.management.security.csrf.controller;

import it.echoeswar.management.common.api.ResponseApi;
import it.echoeswar.management.security.csrf.dto.response.CsrfTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(
        name = "CSRF",
        description = "Operations for obtaining CSRF tokens for browser requests"
)
public class CsrfController {

    private final CsrfTokenRepository csrfTokenRepository;

    public CsrfController(
            CsrfTokenRepository csrfTokenRepository
    ) {
        this.csrfTokenRepository = csrfTokenRepository;
    }

    /**
     * Resolves the deferred CSRF token so that Spring Security issues and persists its cookie.
     *
     * @param request current HTTP request used by the CSRF token repository
     * @param response current HTTP response to which the CSRF cookie is written
     * @return the token value and the header name expected on protected requests
     */
    @Operation(
            summary = "Issue a CSRF token",
            description = "Resolves and returns the CSRF token required to refresh an access token. "
                    + "The response also sets the browser-readable CSRF cookie."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "CSRF token issued successfully",
                    headers = @Header(
                            name = "Set-Cookie",
                            description = "Browser-readable CSRF cookie."
                    ),
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            )
    })
    @GetMapping("/csrf")
    public ResponseEntity<ResponseApi<CsrfTokenResponse>> getCsrfToken(
            @Parameter(hidden = true)
            HttpServletRequest request,
            @Parameter(hidden = true)
            HttpServletResponse response
    ) {
        CsrfToken csrfToken = csrfTokenRepository
                .loadDeferredToken(request, response)
                .get();

        CsrfTokenResponse csrfTokenResponse = new CsrfTokenResponse(
                csrfToken.getToken(),
                csrfToken.getHeaderName()
        );

        return ResponseApi.buildResponse(
                HttpStatus.OK,
                csrfTokenResponse,
                "CSRF token issued successfully."
        );
    }
}
