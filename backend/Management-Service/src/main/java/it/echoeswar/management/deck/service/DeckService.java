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
 * Servizio applicativo responsabile della creazione di un deck completo.
 *
 * <p>Il flusso segue questo ordine: blocca l'utente per gestire il limite dei deck,
 * legge la regola scelta, valida le fazioni e le carte, salva il deck e infine salva
 * le relazioni con fazioni e carte.</p>
 */
@Service
@RequiredArgsConstructor
public class DeckService {

    private static final int MAX_ACTIVE_DECKS_PER_USER = 20;
    // Entrambi gli stati occupano uno dei 20 slot disponibili: un draft non deve aggirare il limite.
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
     * Crea un deck valido per l'utente autenticato.
     *
     * <p>La collezione dell'utente viene soltanto consultata: le copie non vengono consumate
     * né riservate, quindi le stesse carte possono essere utilizzate anche in altri deck.</p>
     *
     * @param userDetails utente autenticato fornito dal layer web
     * @param request regola, metadati, fazioni e carte richieste per il deck
     * @return deck persistito con il riepilogo della sua composizione
     */
    @Transactional
    public DeckResponse createDeck(CustomUserDetails userDetails, CreateDeckRequest request) {

        UUID userId = userDetails.getId();
        // Il lock serializza le creazioni dello stesso utente e rende affidabile il limite di 20 deck.
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Verifica lo slot prima di eseguire tutte le altre query di validazione.
        validateActiveDeckLimit(userId);

        // I limiti del deck (numero carte, costo, copie e fazioni) vengono letti dalla regola persistita.
        DeckRule deckRule = deckRuleRepository.findDeckRuleByActiveTrue()
                .orElseThrow(NotActiveDeckRuleFoundException::new);

        // Il nome è univoco per utente, anche se il deck viene archiviato in seguito.
        if (deckRepository.existsByUserIdAndName(userId, request.name())) {
            throw new ResourceConflictException("A deck with this name already exists.");
        }

        // Prima vengono validate le fazioni dichiarate dal client: saranno le sole ammesse nel deck.
        Map<Byte, Faction> selectedFactionsById = loadSelectedFactions(
                deckRule,
                request.factionIds()
        );

        // La mappa mantiene l'ordine ricevuto e impedisce che una stessa carta compaia due volte nel payload.
        Map<Long, Integer> quantitiesByCardId = normalizeRequestedCards(request.cards());
        // Recupera in una sola query le carte possedute, senza modificare le loro quantità in collezione.
        Map<Long, UserCard> ownedCardsById = loadOwnedCards(userId, quantitiesByCardId);

        // Qui si applicano tutte le regole che dipendono dall'insieme completo delle carte.
        DeckComposition composition = validateComposition(
                deckRule,
                selectedFactionsById,
                quantitiesByCardId,
                ownedCardsById
        );

        // Il deck padre deve essere salvato prima: il suo ID serve nelle chiavi composte delle tabelle figlie.
        Deck deck = deckRepository.saveAndFlush(
                Deck.builder()
                        .user(user)
                        .deckRule(deckRule)
                        .name(FormatterString.normalizeLowerCase(request.name()))
                        .description(FormatterString.normalizeLowerCase(request.description()))
                        // La composizione ha superato tutte le regole, quindi il deck è subito utilizzabile.
                        .status(DeckStatus.READY)
                        .build()
        );

        // Le entità non hanno cascade: le associazioni vengono persistite esplicitamente.
        // Crea una riga in deck_factions per ogni fazione scelta dall'utente.
        List<DeckFaction> deckFactions = composition.selectedFactionsById().values().stream()
                .map(faction -> DeckFaction.builder()
                        .id(new DeckFactionId(deck.getId(), faction.getId()))
                        .deck(deck)
                        .faction(faction)
                        .build())
                .toList();
        deckFactionRepository.saveAll(deckFactions);

        // Crea una riga in deck_cards per ogni carta distinta con la sua quantità validata.
        List<DeckCard> deckCards = composition.cards().stream()
                .map(resolvedCard -> DeckCard.builder()
                        .id(new DeckCardId(deck.getId(), resolvedCard.card().getId()))
                        .deck(deck)
                        .card(resolvedCard.card())
                        .quantity(resolvedCard.quantity())
                        .build())
                .toList();
        deckCardRepository.saveAll(deckCards);

        // La risposta restituisce le fazioni selezionate e le quantità effettivamente salvate.
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
        // I deck ARCHIVED o soft-delete non occupano uno slot; DRAFT e READY sì.
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
        // LinkedHashMap conserva l'ordine del JSON, utile per una risposta e messaggi di errore prevedibili.
        Map<Long, Integer> quantitiesByCardId = new LinkedHashMap<>();
        for (DeckCardRequest requestedCard : requestedCards) {
            // La Bean Validation verifica id e quantità; qui resta il controllo di dominio sui duplicati.
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
        // Non eliminiamo silenziosamente i duplicati: due volte la stessa fazione non valgono come due fazioni.
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

        // Carichiamo tutte le fazioni in blocco e le indicizziamo per ID per individuare quelle mancanti.
        Map<Byte, Faction> availableFactionsById = new LinkedHashMap<>();
        for (Faction faction : factionRepository.findAllById(distinctFactionIds)) {
            availableFactionsById.put(faction.getId(), faction);
        }

        // Ricostruiamo la mappa nell'ordine richiesto e accettiamo solo fazioni attive.
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
        // Una query batch evita di eseguire una query per ogni carta richiesta.
        Map<Long, UserCard> ownedCardsById = new LinkedHashMap<>();
        for (UserCard userCard : userCardRepository.findAllByUserIdAndCardIdIn(
                userId,
                quantitiesByCardId.keySet())
        ) {
            ownedCardsById.put(userCard.getCard().getId(), userCard);
        }

        // Se una carta non è presente nella collezione, il deck non può essere creato.
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
        // Questi valori vengono calcolati sull'intero deck per controllare le regole aggregate.
        long totalCards = 0;
        long totalCost = 0;
        // Serve a verificare che entrambe le fazioni selezionate siano davvero rappresentate da una carta.
        Map<Byte, Faction> factionsUsedByCards = new LinkedHashMap<>();
        List<ResolvedDeckCard> resolvedCards = new ArrayList<>();

        // Analizziamo una riga del payload alla volta: ogni riga rappresenta un tipo di carta e le sue copie.
        for (Map.Entry<Long, Integer> entry : quantitiesByCardId.entrySet()) {
            // 1. Estraiamo ID della carta e quantita richiesta nel JSON.
            long cardId = entry.getKey();
            int requestedQuantity = entry.getValue();

            // 2. Recuperiamo dalla collezione dell'utente la carta e i dati del suo catalogo.
            //    loadOwnedCards ha gia verificato che la carta sia posseduta dall'utente.
            UserCard ownedCard = ownedCardsById.get(cardId);
            Card card = ownedCard.getCard();

            // 3. Verifichiamo che la carta sia attiva, che le copie richieste siano consentite
            //    e che la sua fazione sia una di quelle scelte per questo deck.
            validateCardAvailability(card);
            validateCopyLimit(deckRule, card, ownedCard.getQuantity(), requestedQuantity);
            Faction selectedFaction = selectedFactionsById.get(card.getFaction().getId());
            if (selectedFaction == null) {
                throw new IllegalArgumentException(
                        "Card " + card.getId()
                                + " belongs to a faction that is not selected for this deck."
                );
            }

            // 4. Solo dopo le validazioni aggiorniamo numero totale di carte e costo complessivo.
            //    Il costo della singola carta viene moltiplicato per il numero di copie richieste.
            totalCards += requestedQuantity;
            totalCost += (long) card.getPlayCost() * requestedQuantity;

            // Registriamo la fazione una sola volta: servira al controllo finale sulle fazioni rappresentate.
            factionsUsedByCards.putIfAbsent(card.getFaction().getId(), selectedFaction);

            // Conserviamo carta e quantita risolte per creare successivamente le righe DeckCard da salvare.
            resolvedCards.add(new ResolvedDeckCard(card, requestedQuantity));
        }

        // Regole che dipendono dal deck completo, non dalla singola riga del payload.
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
        // Non si possono inserire carte disattivate, né carte appartenenti a una fazione disattivata.
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
        // Vale il vincolo più restrittivo tra la regola generale del deck e quello specifico della carta.
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
            // Il controllo è per questo deck soltanto: le copie usate in altri deck non vengono sottratte.
            throw new ResourceConflictException(
                    "The user does not own enough copies of card " + card.getId() + "."
            );
        }
    }

}
