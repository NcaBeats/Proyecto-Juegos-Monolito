package com.app.proyectojuegosmonolito.user.controller;

import com.app.proyectojuegosmonolito.user.dto.WalletRequest;
import com.app.proyectojuegosmonolito.user.dto.WalletResponse;
import com.app.proyectojuegosmonolito.user.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/{userId}/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    public ResponseEntity<WalletResponse> findByUserId(@PathVariable Long userId) {
        var wallet = walletService.findByUserId(userId);
        return ResponseEntity.ok(new WalletResponse(wallet.getUserId(), wallet.getBalance(), wallet.getUpdatedAt()));
    }

    @PutMapping
    public ResponseEntity<WalletResponse> updateBalance(@PathVariable Long userId, @Valid @RequestBody WalletRequest request) {
        var wallet = walletService.updateBalance(userId, request.balance());
        return ResponseEntity.ok(new WalletResponse(wallet.getUserId(), wallet.getBalance(), wallet.getUpdatedAt()));
    }
}
