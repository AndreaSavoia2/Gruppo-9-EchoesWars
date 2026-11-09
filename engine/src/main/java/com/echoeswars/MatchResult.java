package com.echoeswars;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
        "matchId",
        "status",
        "winnerUserId",
        "totalRounds",
        "currentLife",
        "rulesVersionId",
        "engineVersion",
        "replay"
})
public class MatchResult {

    private final String matchId;
    private final String rulesVersionId;
    private final String engineVersion;
    private final MatchStatus status;
    private final String winnerId;
    private final int roundsPlayed;
    private final PlayerLife currentLife;
    private final Replay replay;

    public MatchResult(
            String matchId,
            String rulesVersionId,
            String engineVersion,
            MatchStatus status,
            String winnerId,
            int roundsPlayed,
            PlayerLife currentLife,
            Replay replay) {

        // Store match metadata
        this.matchId = matchId;
        this.rulesVersionId = rulesVersionId;
        this.engineVersion = engineVersion;

        // Store the final match outcome
        this.status = status;
        this.winnerId = winnerId;
        this.roundsPlayed = roundsPlayed;

        // Store the final player health values
        this.currentLife = currentLife;

        // Store the match replay
        this.replay = replay;
    }

    public String getMatchId() {
        return matchId;
    }

    public String getRulesVersionId() {
        return rulesVersionId;
    }

    public String getEngineVersion() {
        return engineVersion;
    }

    public MatchStatus getStatus() {
        return status;
    }

    @JsonProperty("winnerUserId")
    public String getWinnerId() {
        return winnerId;
    }

    @JsonProperty("totalRounds")
    public int getRoundsPlayed() {
        return roundsPlayed;
    }

    public PlayerLife getCurrentLife() {
        return currentLife;
    }

    public Replay getReplay() {
        return replay;
    }

}
