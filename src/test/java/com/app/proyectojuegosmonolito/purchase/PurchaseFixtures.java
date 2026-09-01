package com.app.proyectojuegosmonolito.purchase;

import com.app.proyectojuegosmonolito.game.model.Game;
import com.app.proyectojuegosmonolito.purchase.model.Purchase;
import com.app.proyectojuegosmonolito.purchase.model.PurchaseItem;
import com.app.proyectojuegosmonolito.purchase.model.PurchaseStatus;
import com.app.proyectojuegosmonolito.account.user.model.User;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class PurchaseFixtures {

    public static PurchaseItem item(Game game, int quantity) {
        var unitPrice = game.getPrice();
        return PurchaseItem.builder()
                .game(game)
                .unitPrice(unitPrice)
                .quantity(quantity)
                .subtotal(unitPrice.multiply(BigDecimal.valueOf(quantity)))
                .build();
    }

    public static Purchase purchase(User user, PurchaseStatus status, List<PurchaseItem> items) {
        var totalAmount = items.stream()
                .map(PurchaseItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var purchase = Purchase.builder()
                .user(user)
                .totalAmount(totalAmount)
                .status(status)
                .purchasedAt(Instant.now())
                .idempotencyKey("fixture-key-" + user.getUsername() + "-" + Instant.now().toEpochMilli())
                .items(items)
                .build();
        items.forEach(item -> item.setPurchase(purchase));
        return purchase;
    }
}
