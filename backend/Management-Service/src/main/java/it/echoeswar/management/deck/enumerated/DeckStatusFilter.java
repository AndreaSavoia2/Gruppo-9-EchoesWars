package it.echoeswar.management.deck.enumerated;

import java.util.List;

/**
 * Status filters accepted when listing a user's decks.
 *
 * <p>DISABLED decks are deleted and are deliberately never exposed by this filter.</p>
 */
public enum DeckStatusFilter {
    READY(List.of(DeckStatus.READY)),
    ARCHIVED(List.of(DeckStatus.ARCHIVED)),
    ALL(List.of(DeckStatus.READY, DeckStatus.ARCHIVED));

    private final List<DeckStatus> includedStatuses;

    DeckStatusFilter(List<DeckStatus> includedStatuses) {
        this.includedStatuses = List.copyOf(includedStatuses);
    }

    /**
     * Returns the persisted deck statuses included by this filter.
     *
     * @return non-deleted statuses that must be included in the list query
     */
    public List<DeckStatus> getIncludedStatuses() {
        return includedStatuses;
    }
}
