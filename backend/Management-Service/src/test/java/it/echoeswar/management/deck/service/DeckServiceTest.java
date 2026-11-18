package it.echoeswar.management.deck.service;

import it.echoeswar.management.catalog.entity.Card;
import it.echoeswar.management.catalog.entity.Faction;
import it.echoeswar.management.catalog.repository.FactionRepository;
import it.echoeswar.management.collection.entity.UserCard;
import it.echoeswar.management.collection.repository.UserCardRepository;
import it.echoeswar.management.common.exception.ResourceConflictException;
import it.echoeswar.management.deck.entity.Deck;
import it.echoeswar.management.deck.entity.DeckCard;
import it.echoeswar.management.deck.entity.DeckFaction;
import it.echoeswar.management.deck.entity.DeckRule;
import it.echoeswar.management.deck.payload.request.CreateDeckRequest;
import it.echoeswar.management.deck.payload.request.DeckCardRequest;
import it.echoeswar.management.deck.payload.response.DeckResponse;
import it.echoeswar.management.deck.repository.DeckCardRepository;
import it.echoeswar.management.deck.repository.DeckFactionRepository;
import it.echoeswar.management.deck.repository.DeckRepository;
import it.echoeswar.management.deck.repository.DeckRuleRepository;
import it.echoeswar.management.security.userdetails.CustomUserDetails;
import it.echoeswar.management.user.entity.Role;
import it.echoeswar.management.user.entity.User;
import it.echoeswar.management.user.enumerated.AccountStatus;
import it.echoeswar.management.user.enumerated.RoleName;
import it.echoeswar.management.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeckServiceTest {

    private static final long RULE_ID = 1L;

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private DeckRuleRepository deckRuleRepository;

    @Mock
    private DeckCardRepository deckCardRepository;

    @Mock
    private DeckFactionRepository deckFactionRepository;

    @Mock
    private UserCardRepository userCardRepository;

    @Mock
    private FactionRepository factionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DeckService deckService;

    @Test
    void createDeckPersistsAValidThirtyCardDeckWithTwoFactions() {
        UUID userId = UUID.randomUUID();
        User user = testUser(userId);
        DeckRule rule = standardRule();
        List<UserCard> ownedCards = standardCollection();
        CreateDeckRequest request = request("  Fiamme e Ombre  ", ownedCards);

        stubValidCreation(userId, user, rule, ownedCards);
        when(deckRepository.saveAndFlush(any(Deck.class))).thenAnswer(invocation -> {
            Deck deck = invocation.getArgument(0);
            deck.setId(44L);
            return deck;
        });

        DeckResponse response = deckService.createDeck(userDetails(user), request);

        assertThat(response.id()).isEqualTo(44L);
        assertThat(response.deckRuleId()).isEqualTo(RULE_ID);
        assertThat(response.deckRuleCode()).isEqualTo("STANDARD_30");
        assertThat(response.name()).isEqualTo("  Fiamme e Ombre  ");
        assertThat(response.totalCards()).isEqualTo(30);
        assertThat(response.totalCost()).isEqualTo(60);
        assertThat(response.factionIds()).containsExactly((byte) 1, (byte) 2);
        assertThat(response.cards()).hasSize(10)
                .allSatisfy(card -> assertThat(card.quantity()).isEqualTo(3));

        ArgumentCaptor<Deck> deckCaptor = ArgumentCaptor.forClass(Deck.class);
        verify(deckRepository).saveAndFlush(deckCaptor.capture());
        assertThat(deckCaptor.getValue().getUser()).isSameAs(user);
        assertThat(deckCaptor.getValue().getDeckRule()).isSameAs(rule);
        assertThat(deckCaptor.getValue().getName()).isEqualTo("  Fiamme e Ombre  ");

        ArgumentCaptor<Iterable<DeckFaction>> factionsCaptor = iterableCaptor();
        verify(deckFactionRepository).saveAll(factionsCaptor.capture());
        List<DeckFaction> persistedFactions = asList(factionsCaptor.getValue());
        assertThat(persistedFactions).hasSize(2);
        assertThat(persistedFactions)
                .extracting(deckFaction -> deckFaction.getFaction().getId())
                .containsExactly((byte) 1, (byte) 2);
        assertThat(persistedFactions)
                .allSatisfy(deckFaction -> assertThat(deckFaction.getId().getDeckId())
                        .isEqualTo(44L));

        ArgumentCaptor<Iterable<DeckCard>> deckCardsCaptor = iterableCaptor();
        verify(deckCardRepository).saveAll(deckCardsCaptor.capture());
        List<DeckCard> persistedCards = asList(deckCardsCaptor.getValue());
        assertThat(persistedCards).hasSize(10)
                .allSatisfy(deckCard -> {
                    assertThat(deckCard.getId().getDeckId()).isEqualTo(44L);
                    assertThat(deckCard.getQuantity()).isEqualTo(3);
                });
    }

    @Test
    void createDeckAllowsThreeOwnedCopiesToBeUsedInTwoDifferentDecksWithoutMutatingCollection() {
        UUID userId = UUID.randomUUID();
        User user = testUser(userId);
        DeckRule rule = standardRule();
        List<UserCard> ownedCards = standardCollection();

        when(userRepository.findByIdForUpdate(userId)).thenReturn(Optional.of(user));
        when(deckRepository.countByUserIdAndDeletedAtIsNullAndStatusIn(
                eq(userId),
                anyCollection()
        )).thenReturn(0L, 1L);
        when(deckRuleRepository.findById(RULE_ID)).thenReturn(Optional.of(rule));
        when(deckRepository.existsByUserIdAndName(eq(userId), anyString())).thenReturn(false);
        when(factionRepository.findAllById(any())).thenReturn(selectedFactions());
        when(userCardRepository.findAllByUserIdAndCardIdIn(eq(userId), anyCollection()))
                .thenReturn(ownedCards);

        AtomicLong nextDeckId = new AtomicLong(100L);
        when(deckRepository.saveAndFlush(any(Deck.class))).thenAnswer(invocation -> {
            Deck deck = invocation.getArgument(0);
            deck.setId(nextDeckId.getAndIncrement());
            return deck;
        });

        DeckResponse firstDeck = deckService.createDeck(
                userDetails(user),
                request("Primo mazzo", ownedCards)
        );
        DeckResponse secondDeck = deckService.createDeck(
                userDetails(user),
                request("Secondo mazzo", ownedCards)
        );

        assertThat(firstDeck.id()).isEqualTo(100L);
        assertThat(secondDeck.id()).isEqualTo(101L);
        assertThat(firstDeck.totalCards()).isEqualTo(30);
        assertThat(secondDeck.totalCards()).isEqualTo(30);
        assertThat(ownedCards)
                .extracting(UserCard::getQuantity)
                .containsOnly(3);

        verify(userCardRepository, times(2)).findAllByUserIdAndCardIdIn(
                eq(userId),
                anyCollection()
        );
        verify(userCardRepository, never()).save(any(UserCard.class));
        verifyNoMoreInteractions(userCardRepository);

        ArgumentCaptor<Iterable<DeckCard>> deckCardsCaptor = iterableCaptor();
        verify(deckCardRepository, times(2)).saveAll(deckCardsCaptor.capture());
        assertThat(deckCardsCaptor.getAllValues())
                .allSatisfy(deckCards -> assertThat(asList(deckCards)).hasSize(10)
                        .allSatisfy(deckCard -> assertThat(deckCard.getQuantity()).isEqualTo(3)));
    }

    @Test
    void createDeckRejectsTheTwentyFirstActiveDeckBeforeLoadingTheRuleOrCollection() {
        UUID userId = UUID.randomUUID();
        User user = testUser(userId);
        when(userRepository.findByIdForUpdate(userId))
                .thenReturn(Optional.of(user));
        when(deckRepository.countByUserIdAndDeletedAtIsNullAndStatusIn(
                eq(userId),
                anyCollection()
        )).thenReturn(20L);

        assertThatThrownBy(() -> deckService.createDeck(
                userDetails(user),
                new CreateDeckRequest(
                        RULE_ID,
                        "Deck oltre il limite",
                        "Deck di test",
                        List.of((byte) 1, (byte) 2),
                        List.of()
                )
        ))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("A user can have at most 20 active decks.");

        verify(deckRepository, never()).saveAndFlush(any(Deck.class));
        verifyNoInteractions(
                deckRuleRepository,
                userCardRepository,
                factionRepository,
                deckFactionRepository,
                deckCardRepository
        );
    }

    @Test
    void createDeckRejectsCardFromFactionThatIsNotSelectedForTheDeck() {
        UUID userId = UUID.randomUUID();
        User user = testUser(userId);
        DeckRule rule = standardRule();
        List<UserCard> ownedCards = standardCollection();
        ownedCards.get(9).getCard().setFaction(
                Faction.builder().id((byte) 3).active(true).build()
        );

        stubValidCreation(userId, user, rule, ownedCards);

        assertThatThrownBy(() -> deckService.createDeck(
                userDetails(user),
                request("Fiamme e Ombre", ownedCards)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Card 10 belongs to a faction that is not selected for this deck.");

        verify(deckRepository, never()).saveAndFlush(any(Deck.class));
        verifyNoInteractions(deckFactionRepository, deckCardRepository);
    }

    private void stubValidCreation(
            UUID userId,
            User user,
            DeckRule rule,
            List<UserCard> ownedCards
    ) {
        when(userRepository.findByIdForUpdate(userId)).thenReturn(Optional.of(user));
        when(deckRepository.countByUserIdAndDeletedAtIsNullAndStatusIn(
                eq(userId),
                anyCollection()
        )).thenReturn(0L);
        when(deckRuleRepository.findById(RULE_ID)).thenReturn(Optional.of(rule));
        when(deckRepository.existsByUserIdAndName(userId, "Fiamme e Ombre")).thenReturn(false);
        when(factionRepository.findAllById(any())).thenReturn(selectedFactions());
        when(userCardRepository.findAllByUserIdAndCardIdIn(eq(userId), anyCollection()))
                .thenReturn(ownedCards);
    }

    private static DeckRule standardRule() {
        return DeckRule.builder()
                .id(RULE_ID)
                .code("STANDARD_30")
                .exactCardCount(30)
                .maxTotalCost(98)
                .maxCopiesSameCard(3)
                .requiredFactionCount(2)
                .active(true)
                .build();
    }

    private static List<UserCard> standardCollection() {
        Faction firstFaction = Faction.builder().id((byte) 1).active(true).build();
        Faction secondFaction = Faction.builder().id((byte) 2).active(true).build();
        List<UserCard> userCards = new ArrayList<>();

        for (long cardId = 1; cardId <= 10; cardId++) {
            Faction faction = cardId <= 5 ? firstFaction : secondFaction;
            userCards.add(UserCard.builder()
                    .card(Card.builder()
                            .id(cardId)
                            .faction(faction)
                            .playCost((byte) 2)
                            .maxCopyDeck(3)
                            .active(true)
                            .build())
                    .quantity(3)
                    .build());
        }

        return userCards;
    }

    private static CreateDeckRequest request(String name, List<UserCard> ownedCards) {
        return new CreateDeckRequest(
                RULE_ID,
                name,
                "Deck di test",
                List.of((byte) 1, (byte) 2),
                ownedCards.stream()
                        .map(userCard -> new DeckCardRequest(userCard.getCard().getId(), 3))
                        .toList()
        );
    }

    private static List<Faction> selectedFactions() {
        return List.of(
                Faction.builder().id((byte) 1).active(true).build(),
                Faction.builder().id((byte) 2).active(true).build()
        );
    }

    private static User testUser(UUID userId) {
        return User.builder()
                .id(userId)
                .email("deck-service-test@example.com")
                .accountStatus(AccountStatus.ACTIVE)
                .role(Role.builder().roleName(RoleName.ROLE_USER).build())
                .build();
    }

    private static CustomUserDetails userDetails(User user) {
        return CustomUserDetails.forJwtAuthentication(user);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <T> ArgumentCaptor<Iterable<T>> iterableCaptor() {
        return (ArgumentCaptor) ArgumentCaptor.forClass(Iterable.class);
    }

    private static <T> List<T> asList(Iterable<T> values) {
        return StreamSupport.stream(values.spliterator(), false).toList();
    }
}
