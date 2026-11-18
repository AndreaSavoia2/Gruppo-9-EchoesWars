package it.echoeswar.management.pack.service;

import it.echoeswar.management.common.exception.ResourceConflictException;
import it.echoeswar.management.credit.entity.CreditTransaction;
import it.echoeswar.management.credit.entity.CreditWallet;
import it.echoeswar.management.credit.enumerated.TransactionType;
import it.echoeswar.management.credit.repository.CreditTransactionRepository;
import it.echoeswar.management.credit.repository.CreditWalletRepository;
import it.echoeswar.management.pack.entity.GamePackage;
import it.echoeswar.management.pack.entity.PackagePurchase;
import it.echoeswar.management.pack.entity.PackageVersion;
import it.echoeswar.management.pack.payload.response.PackagePurchaseResponse;
import it.echoeswar.management.pack.repository.PackagePurchaseRepository;
import it.echoeswar.management.pack.repository.PackageVersionRepository;
import it.echoeswar.management.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackagePurchaseServiceTest {

    @Mock
    private PackageVersionRepository packageVersionRepository;

    @Mock
    private CreditWalletRepository creditWalletRepository;

    @Mock
    private CreditTransactionRepository creditTransactionRepository;

    @Mock
    private PackagePurchaseRepository packagePurchaseRepository;

    @InjectMocks
    private PackagePurchaseService packagePurchaseService;

    @Test
    void purchasePackageDebitsWalletRecordsTransactionAndKeepsPurchaseUnopened() {
        UUID userId = UUID.randomUUID();
        long packageId = 10L;
        GamePackage gamePackage = GamePackage.builder()
                .id(packageId)
                .code("STARTER")
                .nameIt("Starter")
                .nameEn("Starter")
                .active(true)
                .build();
        PackageVersion packageVersion = PackageVersion.builder()
                .id(20L)
                .gamePackage(gamePackage)
                .versionNumber(3)
                .priceCredits(30L)
                .active(true)
                .build();
        User user = User.builder().id(userId).build();
        CreditWallet wallet = CreditWallet.builder().id(40L).user(user).balance(100L).build();

        when(packageVersionRepository.findAvailableByGamePackageId(packageId))
                .thenReturn(List.of(packageVersion));
        when(creditWalletRepository.findByUserIdForUpdate(userId))
                .thenReturn(Optional.of(wallet));
        when(creditTransactionRepository.saveAndFlush(any(CreditTransaction.class)))
                .thenAnswer(invocation -> {
                    CreditTransaction transaction = invocation.getArgument(0);
                    transaction.setId(50L);
                    return transaction;
                });
        when(packagePurchaseRepository.saveAndFlush(any(PackagePurchase.class)))
                .thenAnswer(invocation -> {
                    PackagePurchase purchase = invocation.getArgument(0);
                    purchase.setId(60L);
                    purchase.setPurchasedAt(LocalDateTime.of(2026, 11, 16, 16, 0));
                    return purchase;
                });

        PackagePurchaseResponse response = packagePurchaseService.purchasePackage(userId, packageId);

        assertThat(wallet.getBalance()).isEqualTo(70L);
        assertThat(response.purchaseId()).isEqualTo(60L);
        assertThat(response.packageId()).isEqualTo(packageId);
        assertThat(response.packageVersionId()).isEqualTo(20L);
        assertThat(response.pricePaidCredits()).isEqualTo(30L);
        assertThat(response.remainingBalance()).isEqualTo(70L);
        assertThat(response.creditTransactionId()).isEqualTo(50L);
        assertThat(response.opened()).isFalse();

        ArgumentCaptor<CreditTransaction> transactionCaptor =
                ArgumentCaptor.forClass(CreditTransaction.class);
        verify(creditTransactionRepository).saveAndFlush(transactionCaptor.capture());
        assertThat(transactionCaptor.getValue().getWallet()).isSameAs(wallet);
        assertThat(transactionCaptor.getValue().getAmount()).isEqualTo(-30L);
        assertThat(transactionCaptor.getValue().getTransactionType())
                .isEqualTo(TransactionType.PACKAGE_PURCHASE);

        ArgumentCaptor<PackagePurchase> purchaseCaptor =
                ArgumentCaptor.forClass(PackagePurchase.class);
        verify(packagePurchaseRepository).saveAndFlush(purchaseCaptor.capture());
        assertThat(purchaseCaptor.getValue().getUser()).isSameAs(user);
        assertThat(purchaseCaptor.getValue().getPackageVersion()).isSameAs(packageVersion);
        assertThat(purchaseCaptor.getValue().getCreditTransaction())
                .isSameAs(transactionCaptor.getValue());
        assertThat(purchaseCaptor.getValue().getPricePaidCredits()).isEqualTo(30L);
    }

    @Test
    void purchasePackageRejectsInsufficientCreditsWithoutPersistingAnything() {
        UUID userId = UUID.randomUUID();
        long packageId = 10L;
        GamePackage gamePackage = GamePackage.builder().id(packageId).active(true).build();
        PackageVersion packageVersion = PackageVersion.builder()
                .id(20L)
                .gamePackage(gamePackage)
                .versionNumber(1)
                .priceCredits(30L)
                .active(true)
                .build();
        CreditWallet wallet = CreditWallet.builder()
                .user(User.builder().id(userId).build())
                .balance(29L)
                .build();

        when(packageVersionRepository.findAvailableByGamePackageId(packageId))
                .thenReturn(List.of(packageVersion));
        when(creditWalletRepository.findByUserIdForUpdate(userId))
                .thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> packagePurchaseService.purchasePackage(userId, packageId))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("Insufficient credits to purchase this package.");

        assertThat(wallet.getBalance()).isEqualTo(29L);
        verifyNoInteractions(creditTransactionRepository, packagePurchaseRepository);
    }
}
