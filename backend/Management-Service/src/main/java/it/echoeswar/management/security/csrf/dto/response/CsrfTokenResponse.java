package it.echoeswar.management.security.csrf.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/** CSRF token and the request header that must carry it. */
@Schema(
        name = "CsrfTokenResponse",
        description = "CSRF token information required by the refresh-token endpoint."
)
public record CsrfTokenResponse(
        @Schema(
                description = "CSRF token value to send in the header identified by headerName when refreshing an access token.",
                example = "2f1e6a0b-2470-4a30-b8a9-96e0322af1e2",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String token,

        @Schema(
                description = "HTTP header name that must contain the CSRF token.",
                example = "X-XSRF-TOKEN",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String headerName
) {
}
