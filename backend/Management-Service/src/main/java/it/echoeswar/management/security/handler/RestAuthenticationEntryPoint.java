package it.echoeswar.management.security.handler;

import tools.jackson.databind.ObjectMapper;
import it.echoeswar.management.common.api.ResponseApi;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Serializes the standard API response for unauthenticated access to protected resources.
 *
 * <p>Spring Security invokes this component before a request reaches a controller.</p>
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authenticationException
    ) throws IOException {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        // Do not expose authentication failures or token details to the client.
        ResponseApi<Void> body = ResponseApi.<Void>builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .error(status.getReasonPhrase())
                .message("Authentication is required to access this resource.")
                .build();

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        objectMapper.writeValue(response.getOutputStream(), body);
    }

}
