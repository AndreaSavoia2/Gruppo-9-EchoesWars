package it.echoeswar.management.pack.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * Account opening-cycle status relative to the guaranteed legendary defined
 * by the historical rule of a specific purchase.
 */
@Schema(
        name = "PackageLegendaryProgressResponse",
        description = "Account progress toward the next guaranteed legendary."
)
@Builder
public record PackageLegendaryProgressResponse(
        @Schema(description = "Identifier of the unopened package purchase.", example = "42")
        long packagePurchaseId,

        @Schema(description = "Identifier of the historical package version.", example = "7")
        long packageVersionId,

        @Schema(description = "Identifier of the catalog package.", example = "7")
        long packageId,

        @Schema(description = "Cycle length defined by the version rule.", example = "10")
        int cycleLength,

        @Schema(description = "Total number of packages already opened by the account.", example = "19")
        long completedOpenings,

        @Schema(description = "Openings already completed in the current cycle.", example = "9")
        int completedOpeningsInCurrentCycle,

        @Schema(
                description = "Number of openings remaining until the guarantee, including the next legendary opening.",
                example = "1"
        )
        int openingsUntilGuaranteedLegendary,

        @Schema(
                description = "Sequence number of the opening that will receive the guaranteed legendary.",
                example = "20"
        )
        long guaranteedOpeningNumber,

        @Schema(
                description = "Whether the selected purchase receives the legendary if opened immediately.",
                example = "true"
        )
        boolean legendaryGuaranteedIfOpenedNext
) {
}
