package it.echoeswar.management.backoffice.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

public record PackUpdateVersionRequest(
        @Schema(description = "unique package id",
                example = "5", nullable = true)
        @Positive
        long packageId,

        @Schema(description = "number pf cards for package",
                example = "5", nullable = true)
        @Positive
        int cardsPerPackage,

        @Schema(description = "garanted number of common cards",
                example = "5", nullable = true)
        @Positive
        int guaranteCommonCards,

        @Schema(description = "garanted number of rare cards",
                example = "3", nullable = true)
        @Positive
        int guaranteRareCards,

        @Schema(description = "number of cycle before have a legendary card in the package",
                example = "5", nullable = true)
        @Positive
        int cycleLength,

        @Schema(description = "percentage of random to have a common card",
                example = "5", nullable = true)
        @Positive
        int  randomCommonThreshold,

        @Schema(description = "percentage of random to have a rare card",
                example = "5", nullable = true)
        @Positive
        int  randomRareThreshold,

        @Schema(description = "Pack price in credits",
                example = "250", nullable = true)
        @Min(0)
        @Max(10000)
        Long priceCredits
) {

}
