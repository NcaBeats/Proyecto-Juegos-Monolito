package com.app.proyectojuegosmonolito.purchase.service;

import com.app.proyectojuegosmonolito.game.service.GameService;
import com.app.proyectojuegosmonolito.library.service.LibraryService;
import com.app.proyectojuegosmonolito.purchase.Purchase;
import com.app.proyectojuegosmonolito.purchase.PurchaseItem;
import com.app.proyectojuegosmonolito.purchase.model.PurchaseStatus;
import com.app.proyectojuegosmonolito.purchase.repository.PurchaseRepository;
import com.app.proyectojuegosmonolito.user.service.UserService;
import com.app.proyectojuegosmonolito.user.service.WalletService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final UserService userService;
    private final GameService gameService;
    private final WalletService walletService;
    private final LibraryService libraryService;

    @Transactional
    public Purchase create(Long userId, List<ItemRequest> items) {
        var user = userService.findById(userId);
        var wallet = walletService.findByUserId(userId);

        var purchase = Purchase.builder()
                .user(user)
                .status(PurchaseStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .purchasedAt(Instant.now())
                .build();

        var purchaseItems = items.stream().map(itemRequest -> {
            var game = gameService.findById(itemRequest.gameId());
            var unitPrice = game.getPrice();
            var subtotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.quantity()));
            return PurchaseItem.builder()
                    .purchase(purchase)
                    .game(game)
                    .unitPrice(unitPrice)
                    .quantity(itemRequest.quantity())
                    .subtotal(subtotal)
                    .build();
        }).toList();

        purchase.setItems(purchaseItems);
        var totalAmount = purchaseItems.stream()
                .map(PurchaseItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        purchase.setTotalAmount(totalAmount);

        if (wallet.getBalance().compareTo(totalAmount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        walletService.updateBalance(userId, wallet.getBalance().subtract(totalAmount));
        purchase.setStatus(PurchaseStatus.COMPLETED);
        var saved = purchaseRepository.save(purchase);

        purchaseItems.forEach(item -> libraryService.add(userId, item.getGame().getId()));

        return saved;
    }

    public Purchase findById(Long id) {
        return purchaseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Purchase not found: " + id));
    }

    public Page<Purchase> findAll(Pageable pageable) {
        return purchaseRepository.findAll(pageable);
    }

    public record ItemRequest(Long gameId, Integer quantity) {}
}
