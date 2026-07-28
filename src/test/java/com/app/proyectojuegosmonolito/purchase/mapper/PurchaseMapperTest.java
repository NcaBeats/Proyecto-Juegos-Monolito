package com.app.proyectojuegosmonolito.purchase.mapper;

import com.app.proyectojuegosmonolito.game.model.Game;
import com.app.proyectojuegosmonolito.purchase.dto.PurchaseItemResponse;
import com.app.proyectojuegosmonolito.purchase.dto.PurchaseResponse;
import com.app.proyectojuegosmonolito.purchase.model.Purchase;
import com.app.proyectojuegosmonolito.purchase.model.PurchaseItem;
import com.app.proyectojuegosmonolito.purchase.model.PurchaseStatus;
import com.app.proyectojuegosmonolito.user.model.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class PurchaseMapperTest {

    private final PurchaseMapper mapper = new PurchaseMapper();

    @Test
    void toResponse_shouldMapAllFields() {
        var purchasedAt = Instant.parse("2026-01-01T00:00:00Z");
        var user = User.builder().id(1L).build();
        var game = Game.builder().id(10L).name("Test Game").build();
        var item = PurchaseItem.builder()
                .id(100L).game(game)
                .unitPrice(new BigDecimal("29.99")).quantity(2)
                .subtotal(new BigDecimal("59.98"))
                .build();
        var purchase = Purchase.builder()
                .id(1L).user(user)
                .totalAmount(new BigDecimal("59.98"))
                .status(PurchaseStatus.COMPLETED).purchasedAt(purchasedAt)
                .items(List.of(item))
                .build();

        var result = mapper.toResponse(purchase);

        assertThat(result).isEqualTo(new PurchaseResponse(1L, 1L,
                new BigDecimal("59.98"), PurchaseStatus.COMPLETED, purchasedAt,
                List.of(new PurchaseItemResponse(100L, 10L, "Test Game",
                        new BigDecimal("29.99"), 2, new BigDecimal("59.98")))));
    }
}
