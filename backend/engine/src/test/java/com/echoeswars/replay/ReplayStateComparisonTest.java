
package com.echoeswars.replay;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReplayStateComparisonTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldDetectDifferentCardState() {

        ReplayCardState first = new ReplayCardState(
                "p1-c1",
                "C001",
                3,
                5,
                1,
                "DOMINIO",
                "GUERRIERI",
                "NESSUNO",
                5,
                5,
                0,
                0,
                0,
                1,
                0,
                null);

        ReplayCardState second = new ReplayCardState(
                "p1-c1",
                "C001",
                3,
                5,
                1,
                "DOMINIO",
                "GUERRIERI",
                "NESSUNO",
                4,
                5,
                0,
                0,
                0,
                1,
                0,
                null);

        assertNotEquals(
                mapper.valueToTree(first),
                mapper.valueToTree(second));
    }

    @Test
    void shouldPreserveDeckOrder() {

        ReplayCardState firstCard = createCard("p1-c1");
        ReplayCardState secondCard = createCard("p1-c2");

        ReplayPlayerState first = new ReplayPlayerState(
                "player-1",
                10,
                null,
                List.of(firstCard, secondCard),
                List.of(),
                null);

        ReplayPlayerState second = new ReplayPlayerState(
                "player-1",
                10,
                null,
                List.of(secondCard, firstCard),
                List.of(),
                null);

        assertNotEquals(
                mapper.valueToTree(first),
                mapper.valueToTree(second));
    }

    private ReplayCardState createCard(String instanceId) {

        return new ReplayCardState(
                instanceId,
                "C001",
                3,
                5,
                1,
                "DOMINIO",
                "GUERRIERI",
                "NESSUNO",
                5,
                5,
                0,
                0,
                0,
                0,
                0,
                null);
    }
}
