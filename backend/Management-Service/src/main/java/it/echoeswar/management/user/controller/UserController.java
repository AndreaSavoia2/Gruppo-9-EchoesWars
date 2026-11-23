package it.echoeswar.management.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.echoeswar.management.common.api.ResponseApi;
import it.echoeswar.management.user.payload.response.UserSearchResponse;
import it.echoeswar.management.user.service.UserService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Exposes user discovery operations to authenticated accounts. */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
@Tag(
        name = "Users",
        description = "Operations for discovering other active users"
)
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Search users by username",
            description = "Returns active, non-deleted users whose username contains the supplied value. "
                    + "The match is case-insensitive."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Matching users retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "The username parameter is missing, blank, or too long",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bearer token is missing, invalid, expired, or revoked",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            )
    })
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/v1/private/users")
    public ResponseEntity<?> searchUsersByUsername(
            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "Complete username or fragment to search for.",
                    example = "andrea",
                    required = true
            )
            @RequestParam("username")
            @NotBlank(message = "Username must not be blank.")
            @NotNull(message = "Username must not be null")
            @Size(max = 50, message = "Username must not exceed 50 characters.")
            String username
    ) {
        List<UserSearchResponse> users = userService.searchUsersByUsername(username);

        if (users.isEmpty()) {
            return ResponseApi.buildResponse(
                    HttpStatus.OK,
                    users,
                    "No users found for the supplied username."
            );
        }

        return ResponseApi.buildResponse(HttpStatus.OK, users);
    }
}
