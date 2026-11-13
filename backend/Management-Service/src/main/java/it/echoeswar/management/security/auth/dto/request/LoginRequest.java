package it.echoeswar.management.security.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
        name = "LoginRequest",
        description = "Credentials used to authenticate an existing user."
)
public record LoginRequest(

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
                description = "Password associated with the email address.",
                example = "CorrectHorseBatteryStaple",
                format = "password",
                accessMode = Schema.AccessMode.WRITE_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Password must not be blank.")
        String password
) {

    @Override
    public String toString() {
        return "LoginRequest[email=" + email
                + ", password=[REDACTED]]";
    }
}
