package it.echoeswar.management.security.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Contains the current credential and the replacement password for an authenticated user.
 */
public record ChangePasswordRequest(

        @NotBlank(message = "Current password must not be blank.")
        String currentPassword,

        @NotBlank(message = "New password must not be blank.")
        String newPassword
) {

    @Override
    public String toString() {
        return "ChangePasswordRequest[currentPassword=[REDACTED], "
                + "newPassword=[REDACTED]]";
    }
}
