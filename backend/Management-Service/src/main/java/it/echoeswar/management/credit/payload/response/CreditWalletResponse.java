package it.echoeswar.management.credit.payload.response;

import it.echoeswar.management.credit.entity.CreditWallet;
import it.echoeswar.management.security.auth.dto.response.UserResponse;
import it.echoeswar.management.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;

// TODO: manca la documentazione

@Builder
public record CreditWalletResponse(
        Long id,
        UserResponse user,
        long balance
) {
    public static CreditWalletResponse from(CreditWallet creditWallet){
        return CreditWalletResponse.builder()
                .id(creditWallet.getId())
                .user(UserResponse.toUserResponse(creditWallet.getUser()))
                .balance(creditWallet.getBalance())
                .build();
    }
}
