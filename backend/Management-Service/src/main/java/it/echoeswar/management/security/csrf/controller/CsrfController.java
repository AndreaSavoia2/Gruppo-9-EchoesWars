package it.echoeswar.management.security.csrf.controller;

import it.echoeswar.management.common.api.ResponseApi;
import it.echoeswar.management.security.csrf.dto.response.CsrfTokenResponse;
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
public class CsrfController {

    private final CsrfTokenRepository csrfTokenRepository;

    public CsrfController(
            CsrfTokenRepository csrfTokenRepository
    ) {
        this.csrfTokenRepository = csrfTokenRepository;
    }

    @GetMapping("/csrf")
    public ResponseEntity<ResponseApi<CsrfTokenResponse>> getCsrfToken(
            HttpServletRequest request,
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
