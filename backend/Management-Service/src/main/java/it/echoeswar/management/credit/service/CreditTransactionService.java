package it.echoeswar.management.credit.service;

import it.echoeswar.management.common.exception.ResourceNotFoundException;
import it.echoeswar.management.common.utils.BuildPageableResponse;
import it.echoeswar.management.credit.entity.CreditTransaction;
import it.echoeswar.management.credit.entity.CreditWallet;
import it.echoeswar.management.credit.enumerated.TransactionType;
import it.echoeswar.management.credit.payload.response.CreditBalanceResponse;
import it.echoeswar.management.credit.payload.response.CreditTransactionResponse;
import it.echoeswar.management.credit.repository.CreditTransactionRepository;
import it.echoeswar.management.credit.repository.CreditWalletRepository;
import it.echoeswar.management.security.userdetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

// TODO: Manca Documentazione completa

@RequiredArgsConstructor
@Service
public class CreditTransactionService {

    private final CreditTransactionRepository creditTransactionRepository;
    private final CreditWalletRepository creditWalletRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getAllTransactionForUser(CustomUserDetails user,
                                                        int pageNumber,
                                                        int pageSize,
                                                        String sortBy,
                                                        String direction){
        Pageable pageable = PageRequest.of(
                pageNumber,
                pageSize,
                Sort.Direction.valueOf(direction.toUpperCase()),
                sortBy);
        return BuildPageableResponse.buildPage(
                creditTransactionRepository.findAllByWalletUserId(user.getId(), pageable)
                        .map(CreditTransactionResponse::from));

    }

    /**
     * Retrieves the current credit balance for the authenticated user.
     *
     * @param user authenticated user whose wallet is being read
     * @return current wallet balance
     * @throws ResourceNotFoundException if the user has no credit wallet
     */
    @Transactional(readOnly = true)
    public CreditBalanceResponse getCurrentCreditBalance(CustomUserDetails user) {
        CreditWallet wallet = creditWalletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Credit wallet",
                        "user id",
                        user.getId()
                ));

        return CreditBalanceResponse.from(wallet);
    }

    @Transactional
    protected CreditTransactionResponse createNewTransaction(long creditAmount,
                                                             UUID userId,
                                                             TransactionType transactionType,
                                                             String description){
        CreditWallet wallet = creditWalletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", "User id", userId));

        wallet.setBalance(creditAmount + wallet.getBalance());
        CreditTransaction creditTransaction = CreditTransaction.builder()
                .wallet(wallet)
                .amount(creditAmount)
                .transactionType(transactionType)
                .description(description)
                .build();
        return CreditTransactionResponse.from(creditTransactionRepository.save(creditTransaction));
    }
}
