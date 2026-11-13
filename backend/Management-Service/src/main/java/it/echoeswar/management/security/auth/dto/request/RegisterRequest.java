package it.echoeswar.management.security.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
        name = "RegisterRequest",
        description = "Data required to create a new user account."
)
public record RegisterRequest(

        @Schema(
                description = "Unique username for the new account.",
                example = "andrea.savoia",
                minLength = 1,
                maxLength = 50,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Username must not be blank.")
        @Size(max = 50, message = "Username must not exceed 50 characters.")
        String username,

        @Schema(
                description = "Email address used to sign in.",
                example = "andrea@example.com",
                format = "email",
                minLength = 1,
                maxLength = 255,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Email must not be blank.")
        @Email(message = "Email must be valid.")
        @Size(max = 255, message = "Email must not exceed 255 characters.")
        String email,

        @Schema(
                description = "Password for the new account. It must contain 12 to 72 characters and no more than 72 UTF-8 bytes.",
                example = "CorrectHorseBatteryStaple",
                format = "password",
                minLength = 12,
                maxLength = 72,
                accessMode = Schema.AccessMode.WRITE_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Password must not be blank.")
        @Size(
                min = 12,
                max = 72,
                message = "Password must contain between 12 and 72 characters."
        )
        String password
) {

    @Override
    public String toString() {
        return "RegisterRequest[username=" + username
                + ", email=" + email
                + ", password=[REDACTED]]";
    }
}
