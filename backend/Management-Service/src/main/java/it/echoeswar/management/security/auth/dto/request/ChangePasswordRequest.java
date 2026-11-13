package it.echoeswar.management.security.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Contains the current credential and the replacement password for an authenticated user.
 */
@Schema(
        name = "ChangePasswordRequest",
        description = "Current and replacement passwords for the authenticated user."
)
public record ChangePasswordRequest(

        @Schema(
                description = "Current account password.",
                example = "CorrectHorseBatteryStaple",
                format = "password",
                accessMode = Schema.AccessMode.WRITE_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Current password must not be blank.")
        String currentPassword,

        @Schema(
                description = "Replacement password. It must differ from the current password, contain at least 12 characters, and use no more than 72 UTF-8 bytes.",
                example = "ASecondCorrectHorseBattery",
                format = "password",
                minLength = 12,
                maxLength = 72,
                accessMode = Schema.AccessMode.WRITE_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "New password must not be blank.")
        String newPassword
) {

    @Override
    public String toString() {
        return "ChangePasswordRequest[currentPassword=[REDACTED], "
                + "newPassword=[REDACTED]]";
    }
}
