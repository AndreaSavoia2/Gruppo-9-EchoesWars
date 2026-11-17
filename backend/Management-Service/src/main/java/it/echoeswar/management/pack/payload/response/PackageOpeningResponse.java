package it.echoeswar.management.pack.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;
import it.echoeswar.management.catalog.entity.Card;
import it.echoeswar.management.catalog.payload.response.CardResponse;
import it.echoeswar.management.pack.entity.PackageOpening;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Esito di un'apertura di pacchetto con le cinque carte estratte.
 */
@Schema(
        name = "PackageOpeningResponse",
        description = "Risultato dell'apertura di un pacchetto, incluse le carte estratte."
)
public record PackageOpeningResponse(
        @Schema(description = "Identificativo dell'apertura appena creata.", example = "18")
        long openingId,

        @Schema(description = "Identificativo dell'acquisto aperto.", example = "42")
        long packagePurchaseId,

        @Schema(description = "Numero progressivo dell'apertura per l'account.", example = "10")
        long openingNumber,

        @Schema(description = "Posizione dell'apertura nel ciclo da dieci.", example = "10")
        short cyclePosition,

        @Schema(description = "Indica se il quinto slot contiene una leggendaria garantita.", example = "true")
        boolean legendaryGuaranteed,

        @Schema(
                description = "Data e ora in cui il pacchetto è stato aperto.",
                example = "2026-11-17T10:15:30",
                format = "date-time"
        )
        LocalDateTime openedAt,

        @Schema(description = "Le cinque carte estratte, nell'ordine degli slot del pacchetto.")
        List<CardResponse> cards
) {

    /**
     * Converte l'apertura persistita e conserva l'ordine delle cinque estrazioni, inclusi i duplicati.
     *
     * @param packageOpening apertura appena persistita
     * @param drawnCards carte estratte nell'ordine dei rispettivi slot
     * @return payload da inviare al client
     */
    public static PackageOpeningResponse from(
            PackageOpening packageOpening,
            List<Card> drawnCards
    ) {
        return new PackageOpeningResponse(
                packageOpening.getId(),
                packageOpening.getPackagePurchase().getId(),
                packageOpening.getOpeningNumber(),
                packageOpening.getCyclePosition(),
                packageOpening.isWasGuaranteed(),
                packageOpening.getOpenedAt(),
                drawnCards.stream().map(CardResponse::from).toList()
        );
    }
}
