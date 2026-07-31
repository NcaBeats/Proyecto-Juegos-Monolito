package com.app.proyectojuegosmonolito.purchase.service;

import com.app.proyectojuegosmonolito.game.service.GameService;
import com.app.proyectojuegosmonolito.library.service.LibraryService;
import com.app.proyectojuegosmonolito.purchase.dto.PurchaseItemRequest;
import com.app.proyectojuegosmonolito.purchase.model.Purchase;
import com.app.proyectojuegosmonolito.purchase.model.PurchaseStatus;
import com.app.proyectojuegosmonolito.purchase.repository.PurchaseRepository;
import com.app.proyectojuegosmonolito.account.user.service.UserService;
import com.app.proyectojuegosmonolito.account.wallet.service.WalletService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.app.proyectojuegosmonolito.game.GameFixtures.*;
import static com.app.proyectojuegosmonolito.account.user.UserFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;
    @Mock
    private UserService userService;
    @Mock
    private GameService gameService;
    @Mock
    private WalletService walletService;
    @Mock
    private LibraryService libraryService;

    @InjectMocks
    private PurchaseService purchaseService;

    @Test
    void create_withSufficientBalance_shouldCompletePurchase() {
        var user = user(1L);
        var wallet = wallet(user, new BigDecimal("100.00"));
        var game1 = game(1L, "Game1", new BigDecimal("29.99"));
        var game2 = game(2L, "Game2", new BigDecimal("49.99"));

        when(userService.findById(1L)).thenReturn(user);
        when(walletService.findByUserId(1L)).thenReturn(wallet);
        when(gameService.findById(1L)).thenReturn(game1);
        when(gameService.findById(2L)).thenReturn(game2);
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(i -> i.getArgument(0));

        var items = List.of(
                new PurchaseItemRequest(1L, 1),
                new PurchaseItemRequest(2L, 1)
        );

        var result = purchaseService.create(1L, items);

        assertThat(result.getStatus()).isEqualTo(PurchaseStatus.COMPLETED);
        assertThat(result.getTotalAmount()).isEqualByComparingTo("79.98");
        assertThat(result.getItems()).hasSize(2);
        assertThat(wallet.getBalance()).isEqualByComparingTo("20.02");
        verify(libraryService).add(1L, 1L);
        verify(libraryService).add(1L, 2L);
        verify(purchaseRepository).save(any(Purchase.class));
    }

    @Test
    void create_withInsufficientBalance_shouldThrow() {
        var user = user(1L);
        var wallet = wallet(user, new BigDecimal("10.00"));
        var game = game(1L, "Game1", new BigDecimal("29.99"));

        when(userService.findById(1L)).thenReturn(user);
        when(walletService.findByUserId(1L)).thenReturn(wallet);
        when(gameService.findById(1L)).thenReturn(game);

        var items = List.of(new PurchaseItemRequest(1L, 1));

        assertThatThrownBy(() -> purchaseService.create(1L, items))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient balance");

        verify(purchaseRepository, never()).save(any());
        verify(libraryService, never()).add(any(), any());
    }

    @Test
    void findById_whenFound_shouldReturnPurchase() {
        var purchase = Purchase.builder()
                .id(1L).totalAmount(BigDecimal.TEN)
                .status(PurchaseStatus.COMPLETED).purchasedAt(Instant.now())
                .build();
        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));

        var result = purchaseService.findById(1L);

        assertThat(result).isEqualTo(purchase);
    }

    @Test
    void findById_whenNotFound_shouldThrow() {
        when(purchaseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseService.findById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void findAll_shouldReturnPage() {
        var pageable = PageRequest.of(0, 10);
        var purchases = List.of(Purchase.builder().id(1L).build());
        var page = new PageImpl<>(purchases, pageable, 1);
        when(purchaseRepository.findAll(pageable)).thenReturn(page);

        var result = purchaseService.findAll(pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findByUserId_shouldReturnPage() {
        var pageable = PageRequest.of(0, 10);
        var purchases = List.of(Purchase.builder().id(1L).build());
        var page = new PageImpl<>(purchases, pageable, 1);
        when(purchaseRepository.findByUser_Id(1L, pageable)).thenReturn(page);

        var result = purchaseService.findByUserId(1L, pageable);

        assertThat(result.getContent()).hasSize(1);
    }
}
