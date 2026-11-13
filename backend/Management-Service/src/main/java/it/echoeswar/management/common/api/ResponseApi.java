package it.echoeswar.management.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard HTTP response envelope returned by controllers and handled errors.
 *
 * <p>The {@code data} field carries the operation-specific payload. Error responses
 * populate {@code error}, {@code message}, and optionally {@code errors} instead.</p>
 *
 * @param <T> type of the operation-specific payload
 */
@Schema(description = "Standard API response envelope.")
@Builder
@Getter
public class ResponseApi<T> {

    @Schema(
            description = "Time at which the response envelope was created.",
            format = "date-time",
            example = "2026-11-13T10:15:30"
    )
    private LocalDateTime timestamp;

    @Schema(
            description = "HTTP status associated with the response.",
            example = "OK"
    )
    private HttpStatus httpStatus;

    @Schema(
            description = "HTTP error reason when the request fails; null for successful responses.",
            example = "Bad Request"
    )
    private String error;

    @Schema(
            description = "Human-readable outcome or error message.",
            example = "Operation completed successfully."
    )
    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(
            description = "Validation errors keyed by request field, present for invalid input.",
            example = "{\"email\":\"Email must be valid.\"}"
    )
    private Map<String, String> errors;

    @Schema(description = "Operation-specific response payload; null when no payload is returned.")
    private T data;

    /**
     * Creates an envelope with a payload and no human-readable message.
     *
     * @param status HTTP status to expose
     * @param data operation-specific payload
     * @param <T> payload type
     * @return a response entity whose status and body use the supplied values
     */
    public static <T> ResponseEntity<ResponseApi<T>> buildResponse(HttpStatus status, T data) {
        ResponseApi<T> response = ResponseApi.<T>builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .data(data)
                .build();
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    /**
     * Creates an envelope with a payload and a human-readable message.
     *
     * @param status HTTP status to expose
     * @param data operation-specific payload
     * @param message human-readable outcome message
     * @param <T> payload type
     * @return a response entity whose status and body use the supplied values
     */
    public static <T> ResponseEntity<ResponseApi<T>> buildResponse(HttpStatus status, T data, String message) {
        ResponseApi<T> response = ResponseApi.<T>builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .message(message)
                .data(data)
                .build();
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    /**
     * Creates an envelope without an operation-specific payload.
     *
     * @param status HTTP status to expose
     * @param message human-readable outcome message
     * @param <T> payload type inferred by the caller
     * @return a response entity whose status and body use the supplied values
     */
    public static <T> ResponseEntity<ResponseApi<T>> buildResponse(HttpStatus status, String message) {
        ResponseApi<T> response = ResponseApi.<T>builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .message(message)
                .build();
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }
}
