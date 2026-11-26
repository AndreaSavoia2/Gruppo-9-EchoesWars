package it.echoeswar.management.social.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;
import it.echoeswar.management.social.entity.Friendship;
import it.echoeswar.management.social.enumerated.FriendshipStatus;
import it.echoeswar.management.user.entity.User;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * API representation of a friendship request.
 *
 * <p>Using this DTO instead of the JPA entity prevents lazy user associations from being
 * serialized and keeps the API response stable.</p>
 *
 * @param id friendship relationship identifier
 * @param requesterUserId identifier of the user who sent the request
 * @param requesterUsername username of the user who sent the request
 * @param recipientUserId identifier of the user who received the request
 * @param recipientUsername username of the user who received the request
 * @param status current lifecycle status of the request
 * @param requestedAt time at which the request was sent
 */
@Schema(
        name = "FriendshipRequestResponse",
        description = "Representation of a friendship request."
)
@Builder
public record FriendshipRequestResponse(
        @Schema(description = "Friendship relationship identifier.", example = "12")
        Long id,

        @Schema(
                description = "Identifier of the user who sent the request.",
                format = "uuid"
        )
        UUID requesterUserId,

        @Schema(
                description = "Username of the user who sent the request.",
                example = "andrea.savoia"
        )
        String requesterUsername,

        @Schema(
                description = "Identifier of the user who received the request.",
                format = "uuid"
        )
        UUID recipientUserId,

        @Schema(
                description = "Username of the user who received the request.",
                example = "mario.rossi"
        )
        String recipientUsername,

        @Schema(description = "Current lifecycle status of the request.", example = "PENDING")
        FriendshipStatus status,

        @Schema(description = "Time at which the request was sent.", format = "date-time")
        LocalDateTime requestedAt
) {

    /**
     * Creates an API response from a friendship and its request recipient.
     *
     * @param friendship friendship relationship to map
     * @param recipient participant who did not send the request
     * @return the mapped friendship-request response
     */
    public static FriendshipRequestResponse from(Friendship friendship, User recipient) {
        User requester = friendship.getRequestedByUser();
        return FriendshipRequestResponse.builder()
                .id(friendship.getId())
                .requesterUserId(requester.getId())
                .requesterUsername(requester.getUsername())
                .recipientUserId(recipient.getId())
                .recipientUsername(recipient.getUsername())
                .status(friendship.getStatus())
                .requestedAt(friendship.getRequestedAt())
                .build();
    }
}
