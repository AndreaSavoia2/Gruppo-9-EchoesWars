package it.echoeswar.management.deck.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;
import it.echoeswar.management.common.utils.FormatterString;
import it.echoeswar.management.deck.entity.Deck;
import it.echoeswar.management.deck.enumerated.DeckStatus;

import java.util.List;

/**
 * Compact deck representation used by list endpoints.
 *
 * <p>It intentionally excludes the cards associated with the deck.</p>
 */
@Schema(
        name = "DeckSummaryResponse",
        description = "Deck information for list views, without associated cards."
)
public record DeckSummaryResponse(

        @Schema(description = "Deck identifier.", example = "42", format = "int64")
        long id,

        @Schema(description = "Deck name.", example = "Fiamme e Ombre")
        String name,

        @Schema(description = "Deck lifecycle state.", example = "READY")
        DeckStatus status,

        @Schema(
                description = "The factions explicitly associated with the deck, including their Italian and English names."
        )
        List<DeckFactionResponse> factions


) {

    /**
     * Preserves backward compatibility for existing calls that build a summary without factions.
     */
    public DeckSummaryResponse(
            long id,
            String name,
            DeckStatus status
    ) {
        this(id, name, status, List.of());
    }

    /**
     * Maps a deck entity to its lightweight API representation.
     *
     * @param deck deck entity to map
     * @param factions factions already loaded and associated with the deck
     * @return summary without cards
     */
    public static DeckSummaryResponse from(
            Deck deck,
            List<DeckFactionResponse> factions
    ) {
        return new DeckSummaryResponse(
                deck.getId(),
                FormatterString.capitalizeFirstLetter(deck.getName()),
                deck.getStatus(),
                List.copyOf(factions)
        );
    }

    /**
     * Version used when factions have not been loaded by the caller.
     */
    public static DeckSummaryResponse from(Deck deck) {
        return from(deck, List.of());
    }
}
