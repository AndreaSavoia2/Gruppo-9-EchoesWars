package it.echoeswar.management.common.exception;


import it.echoeswar.management.common.api.ResponseApi;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ControllerAdvice
public class ExceptionManagement {

    private static final String INVALID_CREDENTIALS_MESSAGE =
            "Invalid email or password.";

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
        // Last node of the property path = the parameter/field name ("registrationUser.arg0" → "arg0").
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
}
