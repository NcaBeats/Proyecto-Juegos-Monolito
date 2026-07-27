package com.app.proyectojuegosmonolito.user.service;

import com.app.proyectojuegosmonolito.TestcontainersConfiguration;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static com.app.proyectojuegosmonolito.user.UserFixtures.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class WalletServiceIT {

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
