package it.echoeswar.management.common.exception;


import it.echoeswar.management.common.api.ResponseApi;
import it.echoeswar.management.deck.exception.NotActiveDeckRuleFoundException;
import it.echoeswar.management.pack.exception.InsufficientCreditsException;
import it.echoeswar.management.security.session.exception.InvalidRefreshTokenException;
import it.echoeswar.management.user.exception.AccountConflictException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class ExceptionManagement {

    private static final String INVALID_CREDENTIALS_MESSAGE =
            "Invalid email or password.";

    private static final String INVALID_REFRESH_TOKEN_MESSAGE =
            "Refresh token is invalid or expired.";

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<?> resourceConflictExceptionManagement(
            ResourceConflictException ex
    ) {
        HttpStatus status = HttpStatus.CONFLICT;

        ResponseApi<?> response = ResponseApi.builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> resourceNotFoundExceptionManagement(ResourceNotFoundException ex){
        HttpStatus status = HttpStatus.NOT_FOUND;

        ResponseApi<?> response = ResponseApi.builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(NotActiveDeckRuleFoundException.class)
    public ResponseEntity<?> notActiveDeckRuleFoundExceptionManagement(
            NotActiveDeckRuleFoundException ex
    ) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        ResponseApi<?> response = ResponseApi.builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(AccountConflictException.class)
    public ResponseEntity<?> accountConflictExceptionManagement(
            AccountConflictException ex
    ) {
        HttpStatus status = HttpStatus.CONFLICT;

        ResponseApi<?> response = ResponseApi.builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(InsufficientCreditsException.class)
    public ResponseEntity<?> insufficientCreditsException(
            InsufficientCreditsException ex
    ) {
        HttpStatus status = HttpStatus.CONFLICT;

        ResponseApi<?> response = ResponseApi.builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> missingServletRequestParameterExceptionManagement(MissingServletRequestParameterException ex){
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ResponseApi<?> response = ResponseApi.builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> methodArgumentTypeMismatchExceptionManagement(MethodArgumentTypeMismatchException ex){
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ResponseApi<?> response = ResponseApi.builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> methodArgumentTypeMismatchExceptionManagement(NoResourceFoundException ex){
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ResponseApi<?> response = ResponseApi.builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<?> constraintViolationExceptionManagement(ConstraintViolationException ex) {
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        // Use the final property-path segment as the parameter or field name.
        Map<String, String> errors = violations.stream()
                .collect(Collectors.toMap(
                        v -> {
                            String path = v.getPropertyPath().toString();
                            return path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
                        },
                        ConstraintViolation::getMessage,
                        (first, second) -> first,
                        LinkedHashMap::new));

        HttpStatus status = HttpStatus.BAD_REQUEST;

        ResponseApi<?> response = ResponseApi.builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .error(status.getReasonPhrase())
                .message("Some fields are invalid.")
                .errors(errors)
                .build();
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<?> handlerMethodValidationExceptionManagement(
            HandlerMethodValidationException ex
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        for (ParameterValidationResult result : ex.getParameterValidationResults()) {
            if (result instanceof ParameterErrors parameterErrors) {
                parameterErrors.getFieldErrors().forEach(fieldError ->
                        errors.putIfAbsent(
                                fieldError.getField(),
                                fieldError.getDefaultMessage() == null
                                        ? "invalid value"
                                        : fieldError.getDefaultMessage()
                        ));
                continue;
            }

            String parameterName = result.getMethodParameter().getParameterName();
            if (parameterName == null) {
                parameterName = "parameter[" + result.getMethodParameter().getParameterIndex() + "]";
            }

            String message = result.getResolvableErrors().stream()
                    .map(MessageSourceResolvable::getDefaultMessage)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse("invalid value");
            errors.putIfAbsent(parameterName, message);
        }

        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        ResponseApi<?> response = ResponseApi.builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .error(status.getReasonPhrase())
                .message(ex.isForReturnValue()
                        ? "The response could not be validated."
                        : "Some fields are invalid.")
                .errors(errors)
                .build();

        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler({
            BadCredentialsException.class,
            DisabledException.class,
            LockedException.class,
            UsernameNotFoundException.class
    })
    public ResponseEntity<?> authenticationFailureExceptionManagement(
            AuthenticationException exception
    ) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        ResponseApi<?> response = ResponseApi.builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .error(status.getReasonPhrase())
                .message(INVALID_CREDENTIALS_MESSAGE)
                .build();

        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<?> invalidRefreshTokenExceptionManagement(
            InvalidRefreshTokenException exception
    ) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        ResponseApi<?> response = ResponseApi.builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .error(status.getReasonPhrase())
                .message(INVALID_REFRESH_TOKEN_MESSAGE)
                .build();

        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<?> handleArgumentNotValidValue(MethodArgumentNotValidException ex) {
        BindingResult bindingResults = ex.getBindingResult();
        Map<String, String> errors = bindingResults
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        org.springframework.validation.FieldError::getField,
                        e -> e.getDefaultMessage() == null ? "invalid value" : e.getDefaultMessage(),
                        (first, second) -> first,
                        LinkedHashMap::new));

        HttpStatus status = HttpStatus.BAD_REQUEST;

        ResponseApi<?> response = ResponseApi.builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .error(status.getReasonPhrase())
                .message("Some fields are invalid.")
                .errors(errors)
                .build();
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<?> propertyReferenceExceptionManagement(PropertyReferenceException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ResponseApi<?> response = ResponseApi.builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> illegalArgumentExceptionManagement(IllegalArgumentException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ResponseApi<?> response = ResponseApi.builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> httpMessageNotReadableExceptionManagement(HttpMessageNotReadableException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ResponseApi<?> response = ResponseApi.builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .error(status.getReasonPhrase())
                .message("Malformed request body.")
                .build();
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> accessDeniedExceptionManagement(AccessDeniedException ex) {
        HttpStatus status = HttpStatus.FORBIDDEN;

        ResponseApi<?> response = ResponseApi.builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .error(status.getReasonPhrase())
                .message("Access denied.")
                .build();
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> genericExceptionManagement(Exception ex) {
        log.error("Unhandled error", ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ResponseApi<?> response = ResponseApi.builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .error(status.getReasonPhrase())
                .message("Internal server error.")
                .build();
        return new ResponseEntity<>(response, status);
    }
}
