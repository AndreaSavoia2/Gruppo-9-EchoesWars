package it.echoeswar.management.user.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;
import it.echoeswar.management.user.entity.User;

import java.util.Objects;
import java.util.UUID;

/**
 * Minimal public representation of an account returned by user search.
 */
@Schema(
        name = "UserSearchResponse",
        description = "Public identity information returned by a username search."
)
public record UserSearchResponse(
        @Schema(
                description = "Unique account identifier.",
                example = "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                format = "uuid",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        UUID id,

        @Schema(
                description = "Username chosen for the account.",
                example = "andrea.savoia",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String username
) {

        public static UserSearchResponse from(User user) {
                return new UserSearchResponse(user.getId(), user.getUsername());
        }
}
