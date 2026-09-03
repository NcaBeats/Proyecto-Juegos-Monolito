package com.app.proyectojuegosmonolito.account.wallet.service;

import com.app.proyectojuegosmonolito.TestcontainersConfiguration;
import com.app.proyectojuegosmonolito.account.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static com.app.proyectojuegosmonolito.account.user.UserFixtures.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Transactional
class WalletServiceIntegrationTest {

    @Autowired
    private WalletService walletService;

    @Autowired
    private UserService userService;

    @Test
    void findByUserId_shouldReturnWallet() {
        var user = userService.create(user());

        var wallet = walletService.findByUserId(user.getId());

        assertThat(wallet.getBalance()).isEqualByComparingTo("0");
    }

    @Test
    void findByUserId_whenNotFound_shouldThrow() {
        assertThatThrownBy(() -> walletService.findByUserId(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateBalance_withPositiveAmount_shouldSucceed() {
        var user = userService.create(user());

        var wallet = walletService.updateBalance(user.getId(), new BigDecimal("50.00"));

        assertThat(wallet.getBalance()).isEqualByComparingTo("50.00");
    }

    @Test
    void updateBalance_withNegativeAmount_shouldThrow() {
        var user = userService.create(user());

        assertThatThrownBy(() -> walletService.updateBalance(user.getId(), new BigDecimal("-10.00")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
