package it.echoeswar.management.pack.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;
import it.echoeswar.management.catalog.entity.Card;
import it.echoeswar.management.catalog.payload.response.CardResponse;
import it.echoeswar.management.pack.entity.PackageOpening;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Result of a package opening with the drawn cards.
 */
@Schema(
        name = "PackageOpeningResponse",
        description = "Result of a package opening, including the drawn cards."
)
public record PackageOpeningResponse(
        @Schema(description = "Identifier of the newly created opening.", example = "18")
        long openingId,

        @Schema(description = "Identifier of the opened purchase.", example = "42")
        long packagePurchaseId,

        @Schema(description = "Sequence number of the opening for the account.", example = "10")
        long openingNumber,

        @Schema(
                description = "Position of the opening in the cycle defined by the purchased version rule.",
                example = "10"
        )
        int cyclePosition,

        @Schema(
                description = "Whether the variable slots contain end-of-cycle guaranteed legendaries.",
                example = "true"
        )
        boolean legendaryGuaranteed,

        @Schema(
                description = "Date and time when the package was opened.",
                example = "2026-11-17T10:15:30",
                format = "date-time"
        )
        LocalDateTime openedAt,

        @Schema(description = "Drawn cards, in the order of the slots defined by the package rule.")
        List<CardResponse> cards
) {

    /**
     * Maps the persisted opening and preserves the draw order, including duplicates.
     *
     * @param packageOpening newly persisted opening
     * @param drawnCards cards drawn in their respective slot order
     * @return payload to send to the client
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
