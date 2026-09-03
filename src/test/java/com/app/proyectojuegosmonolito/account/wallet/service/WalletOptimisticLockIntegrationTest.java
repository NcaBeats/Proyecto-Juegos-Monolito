package com.app.proyectojuegosmonolito.account.wallet.service;

import com.app.proyectojuegosmonolito.TestcontainersConfiguration;
import com.app.proyectojuegosmonolito.account.user.service.UserService;
import com.app.proyectojuegosmonolito.account.wallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;

import static com.app.proyectojuegosmonolito.account.user.UserFixtures.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class WalletOptimisticLockIntegrationTest {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void update_withStaleDetachedWallet_shouldThrowOptimisticLockingFailure() {
        var user = userService.create(user());

        try {
            var stale = transactionTemplate.execute(status ->
                    walletRepository.findById(user.getId()).orElseThrow());
            assertThat(stale).isNotNull();

            transactionTemplate.executeWithoutResult(status -> {
                var wallet = walletRepository.findById(user.getId()).orElseThrow();
                wallet.setBalance(new BigDecimal("100.00"));
            });

            stale.setBalance(new BigDecimal("50.00"));

            assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(status ->
                    walletRepository.save(stale)))
                    .isInstanceOf(OptimisticLockingFailureException.class);
        } finally {
            userService.delete(user.getId());
        }
    }
}
