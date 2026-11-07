package it.echoeswar.management.security.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank(message = "Email must not be blank.")
        @Email(message = "Email must be valid.")
        @Size(max = 255, message = "Email must not exceed 255 characters.")
        String email,

        @NotBlank(message = "Password must not be blank.")
        String password
) {

    @Override
    public String toString() {
        return "LoginRequest[email=" + email
                + ", password=[REDACTED]]";
    }
}
