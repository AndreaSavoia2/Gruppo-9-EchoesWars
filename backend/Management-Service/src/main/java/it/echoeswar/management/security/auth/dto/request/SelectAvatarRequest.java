package it.echoeswar.management.security.auth.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SelectAvatarRequest(

        @NotNull(message = "Avatar id must be provided.")
        @Positive(message = "Avatar id must be positive.")
        Long avatarId
) {
}
