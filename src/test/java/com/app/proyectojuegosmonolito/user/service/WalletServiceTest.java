package com.app.proyectojuegosmonolito.user.service;

import com.app.proyectojuegosmonolito.user.repository.WalletRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static com.app.proyectojuegosmonolito.user.UserFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletService walletService;

    @Test
    void findByUserId_whenFound_shouldReturnWallet() {
        var user = user(1L);
        var wallet = wallet(user, BigDecimal.TEN);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        var result = walletService.findByUserId(1L);

        assertThat(result).isEqualTo(wallet);
    }

    @Test
    void findByUserId_whenNotFound_shouldThrow() {
        when(walletRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> walletService.findByUserId(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateBalance_withPositiveAmount_shouldUpdate() {
        var user = user(1L);
        var wallet = wallet(user, BigDecimal.ZERO);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = walletService.updateBalance(1L, new BigDecimal("50.00"));

        assertThat(result.getBalance()).isEqualByComparingTo("50.00");
        assertThat(result.getUpdatedAt()).isNotNull();
        verify(walletRepository).save(wallet);
    }

    @Test
    void updateBalance_withNegativeAmount_shouldThrow() {
        assertThatThrownBy(() -> walletService.updateBalance(1L, new BigDecimal("-10.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negative");

        verify(walletRepository, never()).findById(any());
        verify(walletRepository, never()).save(any());
    }
}
