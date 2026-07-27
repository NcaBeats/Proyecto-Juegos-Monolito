package com.app.proyectojuegosmonolito.purchase.service;

import com.app.proyectojuegosmonolito.TestcontainersConfiguration;
import com.app.proyectojuegosmonolito.game.repository.GameRepository;
import com.app.proyectojuegosmonolito.purchase.dto.PurchaseItemRequest;
import com.app.proyectojuegosmonolito.user.service.UserService;
import com.app.proyectojuegosmonolito.user.service.WalletService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static com.app.proyectojuegosmonolito.game.GameFixtures.*;
import static com.app.proyectojuegosmonolito.user.UserFixtures.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class PurchaseServiceIT {

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private UserService userService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private GameRepository gameRepository;

    @Test
    void create_withSufficientBalance_shouldCompletePurchase() {
        var user = userService.create(user());
        walletService.updateBalance(user.getId(), new BigDecimal("100.00"));
        var game1 = gameRepository.save(game("Game One", new BigDecimal("29.99")));
        var game2 = gameRepository.save(game("Game Two", new BigDecimal("49.99")));
        var items = List.of(
                new PurchaseItemRequest(game1.getId(), 1),
                new PurchaseItemRequest(game2.getId(), 1)
        );

        var result = purchaseService.create(user.getId(), items);

        assertThat(result.getStatus()).hasToString("COMPLETED");
        assertThat(result.getTotalAmount()).isEqualByComparingTo("79.98");
        assertThat(result.getItems()).hasSize(2);
        var wallet = walletService.findByUserId(user.getId());
        assertThat(wallet.getBalance()).isEqualByComparingTo("20.02");
    }

    @Test
    void create_withInsufficientBalance_shouldThrow() {
        var user = userService.create(user());
        var game = gameRepository.save(game("Game", new BigDecimal("50.00")));
        var items = List.of(new PurchaseItemRequest(game.getId(), 1));

        assertThatThrownBy(() -> purchaseService.create(user.getId(), items))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient balance");
    }

    @Test
    void create_withNonExistentUser_shouldThrow() {
        var items = List.of(new PurchaseItemRequest(1L, 1));

        assertThatThrownBy(() -> purchaseService.create(999L, items))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void create_withNonExistentGame_shouldThrow() {
        var user = userService.create(user());
        var items = List.of(new PurchaseItemRequest(999L, 1));

        assertThatThrownBy(() -> purchaseService.create(user.getId(), items))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void findById_shouldReturnPurchase() {
        var user = userService.create(user());
        walletService.updateBalance(user.getId(), new BigDecimal("100.00"));
        var game = gameRepository.save(game());
        var purchase = purchaseService.create(user.getId(),
                List.of(new PurchaseItemRequest(game.getId(), 1)));

        var found = purchaseService.findById(purchase.getId());

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(purchase.getId());
    }

    @Test
    void findById_whenNotFound_shouldThrow() {
        assertThatThrownBy(() -> purchaseService.findById(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void findAll_shouldReturnPage() {
        var user = userService.create(user());
        walletService.updateBalance(user.getId(), new BigDecimal("100.00"));
        var game = gameRepository.save(game());
        purchaseService.create(user.getId(), List.of(new PurchaseItemRequest(game.getId(), 1)));

        var page = purchaseService.findAll(PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    void findByUserId_shouldReturnPage() {
        var user = userService.create(user());
        walletService.updateBalance(user.getId(), new BigDecimal("100.00"));
        var game = gameRepository.save(game());
        purchaseService.create(user.getId(), List.of(new PurchaseItemRequest(game.getId(), 1)));

        var page = purchaseService.findByUserId(user.getId(), PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
    }
}
