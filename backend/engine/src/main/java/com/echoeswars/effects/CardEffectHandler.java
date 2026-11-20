package com.echoeswars.effects;

public interface CardEffectHandler {

    /**
     * Called when the source card enters the battlefield.
     */
    default void onEntry(EffectContext context) {
        // No entry effect by default.
    }

    /**
     * Called at the beginning of a round.
     */
    default void onRoundStart(EffectContext context) {
        // No round-start effect by default.
    }

    /**
     * Called after combat has been resolved.
     */
    default void onAfterCombat(EffectContext context) {
        // No post-combat effect by default.
    }

    /**
     * Called at the end of a round.
     */
    default void onRoundEnd(EffectContext context) {
        // No end-of-round effect by default.
    }

    default void onDestroyed(EffectContext context) {
    }
}
