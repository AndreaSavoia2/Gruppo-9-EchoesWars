package it.echoeswar.management.security.csrf.dto.response;

public record CsrfTokenResponse(
        String token,
        String headerName
) {
}
