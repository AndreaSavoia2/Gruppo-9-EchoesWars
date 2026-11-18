package it.echoeswar.management.deck.service;

import it.echoeswar.management.catalog.entity.Card;
import it.echoeswar.management.catalog.entity.Faction;
import it.echoeswar.management.catalog.repository.FactionRepository;
import it.echoeswar.management.collection.entity.UserCard;
import it.echoeswar.management.collection.repository.UserCardRepository;
import it.echoeswar.management.common.exception.ResourceConflictException;
import it.echoeswar.management.common.exception.ResourceNotFoundException;
import it.echoeswar.management.common.utils.FormatterString;
import it.echoeswar.management.deck.exception.NotActiveDeckRuleFoundException;
import it.echoeswar.management.deck.entity.Deck;
import it.echoeswar.management.deck.entity.DeckCard;
import it.echoeswar.management.deck.entity.DeckCardId;
import it.echoeswar.management.deck.entity.DeckFaction;
import it.echoeswar.management.deck.entity.DeckFactionId;
import it.echoeswar.management.deck.entity.DeckRule;
import it.echoeswar.management.deck.enumerated.DeckStatus;
import it.echoeswar.management.deck.model.DeckComposition;
import it.echoeswar.management.deck.model.ResolvedDeckCard;
import it.echoeswar.management.deck.payload.request.CreateDeckRequest;
import it.echoeswar.management.deck.payload.request.DeckCardRequest;
import it.echoeswar.management.deck.payload.response.DeckCardResponse;
import it.echoeswar.management.deck.payload.response.DeckResponse;
import it.echoeswar.management.deck.repository.DeckCardRepository;
import it.echoeswar.management.deck.repository.DeckFactionRepository;
import it.echoeswar.management.deck.repository.DeckRepository;
import it.echoeswar.management.deck.repository.DeckRuleRepository;
import it.echoeswar.management.security.userdetails.CustomUserDetails;
import it.echoeswar.management.user.entity.User;
import it.echoeswar.management.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Application service responsible for creating a complete deck.
 *
 * <p>The flow follows this order: it locks the user to enforce the deck limit,
 * loads the active rule, validates factions and cards, saves the deck, and finally
 * saves the faction and card relationships.</p>
 */
@Service
@RequiredArgsConstructor
public class DeckService {

    private static final int MAX_ACTIVE_DECKS_PER_USER = 20;
    // Both statuses occupy one of the 20 available slots: a draft must not bypass the limit.
    private static final List<DeckStatus> ACTIVE_DECK_STATUSES = List.of(
            DeckStatus.DRAFT,
            DeckStatus.READY
    );

    private final DeckRepository deckRepository;
    private final DeckRuleRepository deckRuleRepository;
    private final DeckCardRepository deckCardRepository;
    private final DeckFactionRepository deckFactionRepository;
    private final UserCardRepository userCardRepository;
    private final FactionRepository factionRepository;
    private final UserRepository userRepository;

    /**
     * Creates a valid deck for the authenticated user.
     *
     * <p>The user's collection is only read: copies are neither consumed nor reserved, so the
     * same cards can also be used in other decks.</p>
     *
     * @param userDetails authenticated user provided by the web layer
     * @param request requested deck metadata, factions, and cards
     * @return persisted deck with a summary of its composition
     */
    @Transactional
    public DeckResponse createDeck(CustomUserDetails userDetails, CreateDeckRequest request) {

        UUID userId = userDetails.getId();
        // The lock serializes creations for the same user and makes the 20-deck limit reliable.
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Check the available slot before executing all other validation queries.
        validateActiveDeckLimit(userId);

        // Deck limits (card count, cost, copies, and factions) are read from the active persisted rule.
        DeckRule deckRule = deckRuleRepository.findDeckRuleByActiveTrue()
                .orElseThrow(NotActiveDeckRuleFoundException::new);

        // The name is unique per user, even if the deck is later archived.
        if (deckRepository.existsByUserIdAndName(userId, request.name())) {
            throw new ResourceConflictException("A deck with this name already exists.");
        }

        // First validate the factions declared by the client: they will be the only ones allowed in the deck.
        Map<Byte, Faction> selectedFactionsById = loadSelectedFactions(
                deckRule,
                request.factionIds()
        );

        // The map preserves the received order and prevents the same card from appearing twice in the payload.
        Map<Long, Integer> quantitiesByCardId = normalizeRequestedCards(request.cards());
        // Fetch owned cards with a single query, without changing their quantities in the collection.
        Map<Long, UserCard> ownedCardsById = loadOwnedCards(userId, quantitiesByCardId);

        // Apply all rules that depend on the complete set of cards.
        DeckComposition composition = validateComposition(
                deckRule,
                selectedFactionsById,
                quantitiesByCardId,
                ownedCardsById
        );

        // The parent deck must be saved first because its ID is required by the child-table composite keys.
        Deck deck = deckRepository.saveAndFlush(
                Deck.builder()
                        .user(user)
                        .deckRule(deckRule)
                        .name(FormatterString.normalizeLowerCase(request.name()))
                        .description(FormatterString.normalizeLowerCase(request.description()))
        // The composition passed all rules, so the deck can be used immediately.
                        .status(DeckStatus.READY)
                        .build()
        );

        // The entities have no cascade, so associations are persisted explicitly.
        // Create one deck_factions row for each faction selected by the user.
        List<DeckFaction> deckFactions = composition.selectedFactionsById().values().stream()
                .map(faction -> DeckFaction.builder()
                        .id(new DeckFactionId(deck.getId(), faction.getId()))
                        .deck(deck)
                        .faction(faction)
                        .build())
                .toList();
        deckFactionRepository.saveAll(deckFactions);

        // Create one deck_cards row for each distinct card with its validated quantity.
        List<DeckCard> deckCards = composition.cards().stream()
                .map(resolvedCard -> DeckCard.builder()
                        .id(new DeckCardId(deck.getId(), resolvedCard.card().getId()))
                        .deck(deck)
                        .card(resolvedCard.card())
                        .quantity(resolvedCard.quantity())
                        .build())
                .toList();
        deckCardRepository.saveAll(deckCards);

        // The response returns the selected factions and the quantities actually saved.
        return DeckResponse.builder()
                .id(deck.getId())
                .deckRuleId(deckRule.getId())
                .deckRuleCode(deckRule.getCode())
                .name(FormatterString.capitalizeFirstLetter(deck.getName()))
                .description(FormatterString.capitalizeFirstLetter(deck.getDescription()))
                .status(deck.getStatus())
                .totalCards(composition.totalCards())
                .totalCost(composition.totalCost())
                .factionIds(List.copyOf(composition.selectedFactionsById().keySet()))
                .cards(composition.cards().stream()
                        .map(resolvedCard -> new DeckCardResponse(
                                resolvedCard.card().getId(),
                                resolvedCard.card().getFaction().getId(),
                                resolvedCard.quantity()
                        ))
                        .toList())
                .build();
    }

    private void validateActiveDeckLimit(UUID userId) {
        // ARCHIVED or soft-deleted decks do not occupy a slot; DRAFT and READY decks do.
        long activeDeckCount = deckRepository.countByUserIdAndDeletedAtIsNullAndStatusIn(
                userId,
                ACTIVE_DECK_STATUSES
        );
        if (activeDeckCount >= MAX_ACTIVE_DECKS_PER_USER) {
            throw new ResourceConflictException(
                    "A user can have at most " + MAX_ACTIVE_DECKS_PER_USER + " active decks."
            );
        }
    }

    private Map<Long, Integer> normalizeRequestedCards(List<DeckCardRequest> requestedCards) {
        // LinkedHashMap preserves JSON order, which makes responses and error messages predictable.
        Map<Long, Integer> quantitiesByCardId = new LinkedHashMap<>();
        for (DeckCardRequest requestedCard : requestedCards) {
            // Bean Validation verifies IDs and quantities; only the domain-level duplicate check remains here.
            Integer previousQuantity = quantitiesByCardId.putIfAbsent(
                    requestedCard.cardId(),
                    requestedCard.quantity()
            );
            if (previousQuantity != null) {
                throw new IllegalArgumentException(
                        "A deck card may be specified only once: " + requestedCard.cardId() + "."
                );
            }
        }

        return quantitiesByCardId;
    }

    private Map<Byte, Faction> loadSelectedFactions(DeckRule deckRule, List<Byte> requestedFactionIds) {
        // Do not silently remove duplicates: the same faction twice does not count as two factions.
        Set<Byte> distinctFactionIds = new LinkedHashSet<>(requestedFactionIds);
        if (distinctFactionIds.size() != requestedFactionIds.size()) {
            throw new IllegalArgumentException("Deck factions must be distinct.");
        }
        if (distinctFactionIds.size() != deckRule.getRequiredFactionCount()) {
            throw new IllegalArgumentException(
                    "Deck must be assigned exactly "
                            + deckRule.getRequiredFactionCount()
                            + " factions."
            );
        }

        // Load all factions in bulk and index them by ID to identify missing ones.
        Map<Byte, Faction> availableFactionsById = new LinkedHashMap<>();
        for (Faction faction : factionRepository.findAllById(distinctFactionIds)) {
            availableFactionsById.put(faction.getId(), faction);
        }

        // Rebuild the map in the requested order and accept active factions only.
        Map<Byte, Faction> selectedFactionsById = new LinkedHashMap<>();
        for (Byte factionId : distinctFactionIds) {
            Faction faction = availableFactionsById.get(factionId);
            if (faction == null)
                throw new ResourceNotFoundException("Faction", "id", factionId);

            if (!faction.isActive())
                throw new ResourceConflictException(
                        "Faction " + factionId + " is not available for deck creation.");

            selectedFactionsById.put(factionId, faction);
        }

        return selectedFactionsById;
    }

    private Map<Long, UserCard> loadOwnedCards(UUID userId, Map<Long, Integer> quantitiesByCardId
    ) {
        // A batch query avoids executing one query for every requested card.
        Map<Long, UserCard> ownedCardsById = new LinkedHashMap<>();
        for (UserCard userCard : userCardRepository.findAllByUserIdAndCardIdIn(
                userId,
                quantitiesByCardId.keySet())
        ) {
            ownedCardsById.put(userCard.getCard().getId(), userCard);
        }

        // If a card is not in the collection, the deck cannot be created.
        for (Long cardId : quantitiesByCardId.keySet()) {
            if (!ownedCardsById.containsKey(cardId)) {
                throw new ResourceConflictException(
                        "Card " + cardId + " is not available in the user's collection."
                );
            }
        }

        return ownedCardsById;
    }

    private DeckComposition validateComposition(
            DeckRule deckRule,
            Map<Byte, Faction> selectedFactionsById,
            Map<Long, Integer> quantitiesByCardId,
            Map<Long, UserCard> ownedCardsById
    ) {
        // These values are calculated across the whole deck to validate aggregate rules.
        long totalCards = 0;
        long totalCost = 0;
        // Used to verify that both selected factions are actually represented by at least one card.
        Map<Byte, Faction> factionsUsedByCards = new LinkedHashMap<>();
        List<ResolvedDeckCard> resolvedCards = new ArrayList<>();

        // Process one payload entry at a time: each entry represents one card type and its copies.
        for (Map.Entry<Long, Integer> entry : quantitiesByCardId.entrySet()) {
            // 1. Extract the card ID and requested quantity from the JSON payload.
            long cardId = entry.getKey();
            int requestedQuantity = entry.getValue();

            // 2. Retrieve the card from the user's collection and its catalog data.
            //    loadOwnedCards has already verified that the user owns this card.
            UserCard ownedCard = ownedCardsById.get(cardId);
            Card card = ownedCard.getCard();

            // 3. Verify that the card is active, the requested copies are allowed,
            //    and its faction is one of those selected for this deck.
            validateCardAvailability(card);
            validateCopyLimit(deckRule, card, ownedCard.getQuantity(), requestedQuantity);
            Faction selectedFaction = selectedFactionsById.get(card.getFaction().getId());
            if (selectedFaction == null) {
                throw new IllegalArgumentException(
                        "Card " + card.getId()
                                + " belongs to a faction that is not selected for this deck."
                );
            }

            // 4. Update the total card count and total cost only after validation succeeds.
            //    The cost of one card is multiplied by the number of requested copies.
            totalCards += requestedQuantity;
            totalCost += (long) card.getPlayCost() * requestedQuantity;

            // Record the faction only once; it will be used by the final represented-faction check.
            factionsUsedByCards.putIfAbsent(card.getFaction().getId(), selectedFaction);

            // Keep the resolved card and quantity to create the DeckCard rows saved later.
            resolvedCards.add(new ResolvedDeckCard(card, requestedQuantity));
        }

        // Rules that depend on the complete deck, rather than on a single payload entry.
        if (totalCards != deckRule.getExactCardCount()) {
            throw new IllegalArgumentException(
                    "Deck must contain exactly " + deckRule.getExactCardCount() + " cards."
            );
        }
        if (totalCost > deckRule.getMaxTotalCost()) {
            throw new IllegalArgumentException(
                    "Deck total cost must not exceed " + deckRule.getMaxTotalCost() + "."
            );
        }
        if (factionsUsedByCards.size() != selectedFactionsById.size()) {
            throw new IllegalArgumentException(
                    "Deck must contain at least one card from each selected faction."
            );
        }

        return new DeckComposition(
                Math.toIntExact(totalCards),
                Math.toIntExact(totalCost),
                selectedFactionsById,
                List.copyOf(resolvedCards)
        );
    }

    private void validateCardAvailability(Card card) {
        // Disabled cards, or cards belonging to disabled factions, cannot be included.
        if (!card.isActive() || !card.getFaction().isActive()) {
            throw new ResourceConflictException(
                    "Card " + card.getId() + " is not available for deck creation."
            );
        }
    }

    private void validateCopyLimit(
            DeckRule deckRule,
            Card card,
            int ownedQuantity,
            int requestedQuantity
    ) {
        // The stricter limit between the general deck rule and the card-specific rule applies.
        int maxCopiesForDeck = Math.min(
                deckRule.getMaxCopiesSameCard(),
                card.getMaxCopyDeck()
        );

        if (requestedQuantity > maxCopiesForDeck) {
            throw new IllegalArgumentException(
                    "Card " + card.getId() + " may appear at most "
                            + maxCopiesForDeck
                            + " times in this deck."
            );
        }
        if (requestedQuantity > ownedQuantity) {
        // This check applies only to this deck: copies used in other decks are not deducted.
            throw new ResourceConflictException(
                    "The user does not own enough copies of card " + card.getId() + "."
            );
        }
    }

}
