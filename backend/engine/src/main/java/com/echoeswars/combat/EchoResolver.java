
package com.echoeswars.combat;

import com.echoeswars.model.Card;
import com.echoeswars.model.Echo;

public final class EchoResolver {

    private EchoResolver() {
        // Utility class
    }

    public static int calculateEchoBonus(
            Echo attackerEcho,
            Echo defenderEcho) {

        if (attackerEcho == null || defenderEcho == null) {
            throw new IllegalArgumentException(
                    "Card echoes cannot be null.");
        }

        // Same echo: no attack bonus
        if (attackerEcho == defenderEcho) {
            return 0;
        }

        // Reciprocal weaknesses
        if ((attackerEcho == Echo.DOMINIO
                && defenderEcho == Echo.OBLIO)
                || (attackerEcho == Echo.OBLIO
                && defenderEcho == Echo.DOMINIO)) {

            return 1;
        }

        if ((attackerEcho == Echo.SAPIENZA
                && defenderEcho == Echo.INGANNO)
                || (attackerEcho == Echo.INGANNO
                && defenderEcho == Echo.SAPIENZA)) {

            return 1;
        }

        // Cyclic advantages
        return switch (attackerEcho) {

            case DOMINIO ->
                    defenderEcho == Echo.SAPIENZA ? 1 : 0;

            case SAPIENZA ->
                    defenderEcho == Echo.OBLIO ? 1 : 0;

            case OBLIO ->
                    defenderEcho == Echo.INGANNO ? 1 : 0;

            case INGANNO ->
                    defenderEcho == Echo.DOMINIO ? 1 : 0;
        };
    }

    public static int calculateEchoBonus(
            Card attacker,
            Card defender) {

        if (attacker == null || defender == null) {
            throw new IllegalArgumentException(
                    "Combat cards cannot be null.");
        }

        return calculateEchoBonus(
                attacker.getEcho(),
                defender.getEcho());
    }
}
