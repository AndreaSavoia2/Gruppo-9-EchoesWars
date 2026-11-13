package it.echoeswar.management.security.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Identifies an avatar already unlocked by the authenticated user. */
@Schema(
        name = "SelectAvatarRequest",
        description = "Request to select one of the authenticated user's unlocked avatars."
)
public record SelectAvatarRequest(

        @Schema(
                description = "Positive identifier of an avatar already unlocked by the authenticated user.",
                example = "42",
                format = "int64",
                minimum = "1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Avatar id must be provided.")
        @Positive(message = "Avatar id must be positive.")
        Long avatarId
) {
}
