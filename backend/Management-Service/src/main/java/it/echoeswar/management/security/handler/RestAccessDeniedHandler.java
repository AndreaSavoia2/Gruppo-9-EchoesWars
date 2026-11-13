package it.echoeswar.management.security.handler;

import it.echoeswar.management.common.api.ResponseApi;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Serializes the standard API response when an authenticated principal is denied access.
 *
 * <p>This is an authorization failure and returns HTTP 403.</p>
 */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RestAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        HttpStatus status = HttpStatus.FORBIDDEN;
        // Do not disclose required roles, permissions, or authorization rules.
        ResponseApi<Void> body = ResponseApi.<Void>builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .error(status.getReasonPhrase())
                .message("You do not have permission to access this resource.")
                .build();

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}