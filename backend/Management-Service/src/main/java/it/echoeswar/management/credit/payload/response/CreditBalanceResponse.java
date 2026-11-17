package it.echoeswar.management.credit.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;
import it.echoeswar.management.credit.entity.CreditWallet;

/**
 * Current credit balance of the authenticated user's wallet.
 */
@Schema(
        name = "CreditBalanceResponse",
        description = "Current credit balance of the authenticated user's wallet."
)
public record CreditBalanceResponse(
        @Schema(
                description = "Credits currently available to the authenticated user.",
                example = "250",
                format = "int64",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        long balance
) {

    /**
     * Maps a wallet to its public balance representation.
     *
     * @param creditWallet persisted wallet
     * @return current wallet balance
     */
    public static CreditBalanceResponse from(CreditWallet creditWallet) {
        return new CreditBalanceResponse(creditWallet.getBalance());
    }
}
