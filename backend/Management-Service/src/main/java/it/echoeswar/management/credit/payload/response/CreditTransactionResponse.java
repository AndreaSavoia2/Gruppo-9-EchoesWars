package it.echoeswar.management.credit.payload.response;

import it.echoeswar.management.common.utils.FormatterString;
import it.echoeswar.management.credit.entity.CreditTransaction;
import it.echoeswar.management.credit.entity.CreditWallet;
import it.echoeswar.management.credit.enumerated.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
public record CreditTransactionResponse(
        Long id,
        long amount,
        String description,
        LocalDateTime createdAt,
        TransactionType transactionType
) {

    public static CreditTransactionResponse from(CreditTransaction creditTransaction){
        return CreditTransactionResponse.builder()
                .id(creditTransaction.getId())
                .amount(creditTransaction.getAmount())
                .description(FormatterString.capitalizeFirstLetter(creditTransaction.getDescription()))
                .createdAt(creditTransaction.getCreatedAt())
                .transactionType(creditTransaction.getTransactionType())
                .build();
    }
}
