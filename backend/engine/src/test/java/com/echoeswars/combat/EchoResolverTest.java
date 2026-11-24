
package com.echoeswars.combat;

import com.echoeswars.model.Echo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EchoResolverTest {

    @Test
    void dominioShouldBeatSapienza() {
        int bonus = EchoResolver.calculateEchoBonus(
                Echo.DOMINIO,
                Echo.SAPIENZA
        );

        assertEquals(1, bonus);
    }

    @Test
    void sapienzaShouldNotBeatDominio() {
        int bonus = EchoResolver.calculateEchoBonus(
                Echo.SAPIENZA,
                Echo.DOMINIO
        );

        assertEquals(0, bonus);
    }

    @Test
    void dominioAndOblioShouldBothGetBonus() {
        assertEquals(1, EchoResolver.calculateEchoBonus(
                Echo.DOMINIO, Echo.OBLIO));

        assertEquals(1, EchoResolver.calculateEchoBonus(
                Echo.OBLIO, Echo.DOMINIO));
    }

    @Test
    void sameEchoShouldNotGiveBonus() {
        for (Echo echo : Echo.values()) {
            assertEquals(0,
                    EchoResolver.calculateEchoBonus(echo, echo));
        }
    }
}
