package it.echoeswar.management.security.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import it.echoeswar.management.user.entity.User;
import it.echoeswar.management.user.enumerated.AccountStatus;
import it.echoeswar.management.user.enumerated.RoleName;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(
        name = "UserResponse",
        description = "Public account details returned by authentication and profile endpoints."
)
public record UserResponse(
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
        String username,

        @Schema(
                description = "Email address associated with the account.",
                example = "andrea@example.com",
                format = "email",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String email,

        @Schema(
                description = "Role assigned to the user.",
                example = "ROLE_USER",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        RoleName role,

        @Schema(
                description = "Current account lifecycle status.",
                example = "ACTIVE",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        AccountStatus accountStatus,

        @Schema(
                description = "Identifier of the selected avatar, or null when no avatar has been selected.",
                example = "42",
                format = "int64",
                nullable = true,
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Long selectedAvatarId,

        @Schema(
                description = "Account creation date and time, serialized without a UTC offset.",
                example = "2026-11-13T10:15:30",
                format = "date-time",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        LocalDateTime createdAt
) {

    /**
     * Maps a persisted user to the public account representation.
     *
     * @param user persisted account with its role loaded
     * @return public account details without credentials or internal lifecycle data
     */
    public static UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().getRoleName(),
                user.getAccountStatus(),
                user.getSelectedAvatar() == null
                        ? null
                        : user.getSelectedAvatar().getId(),
                user.getCreatedAt()
        );
    }
}
