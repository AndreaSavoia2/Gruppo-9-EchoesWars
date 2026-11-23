package it.echoeswar.management.social.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;
import it.echoeswar.management.social.enumerated.FriendshipStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Dati restituiti per una richiesta di amicizia.
 *
 * <p>Esporre un DTO, invece dell'entità JPA, evita di serializzare le relazioni
 * lazy verso gli utenti e mantiene la risposta dell'API stabile.</p>
 */
@Schema(
        name = "FriendshipRequestResponse",
        description = "Rappresentazione di una richiesta di amicizia."
)
public record FriendshipRequestResponse(
        @Schema(description = "Identificativo della relazione di amicizia.", example = "12")
        Long id,

        @Schema(
                description = "Identificativo dell'utente che ha inviato la richiesta.",
                format = "uuid"
        )
        UUID requesterUserId,

        @Schema(
                description = "Username dell'utente che ha inviato la richiesta.",
                example = "andrea.savoia"
        )
        String requesterUsername,

        @Schema(
                description = "Identificativo dell'utente che deve ricevere la richiesta.",
                format = "uuid"
        )
        UUID recipientUserId,

        @Schema(
                description = "Username dell'utente che deve ricevere la richiesta.",
                example = "mario.rossi"
        )
        String recipientUsername,

        @Schema(description = "Stato iniziale della richiesta.", example = "PENDING")
        FriendshipStatus status,

        @Schema(description = "Istante in cui è stata inviata la richiesta.", format = "date-time")
        LocalDateTime requestedAt
) {
}
