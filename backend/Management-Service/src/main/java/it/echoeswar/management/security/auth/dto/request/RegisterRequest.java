package it.echoeswar.management.security.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Username must not be blank.")
        @Size(max = 50, message = "Username must not exceed 50 characters.")
        String username,

        @NotBlank(message = "Email must not be blank.")
        @Email(message = "Email must be valid.")
        @Size(max = 255, message = "Email must not exceed 255 characters.")
        String email,

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
