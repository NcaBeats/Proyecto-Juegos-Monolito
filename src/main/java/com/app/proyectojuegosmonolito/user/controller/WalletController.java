package com.app.proyectojuegosmonolito.user.controller;

import com.app.proyectojuegosmonolito.user.dto.WalletRequest;
import com.app.proyectojuegosmonolito.user.dto.WalletResponse;
import com.app.proyectojuegosmonolito.user.mapper.WalletMapper;
import com.app.proyectojuegosmonolito.user.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Wallet", description = "Wallet management APIs")
@RestController
@RequestMapping("/api/v1/users/{userId}/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final WalletMapper walletMapper;

    @Operation(summary = "Get wallet by user", description = "Returns the wallet for a specific user")
    @ApiResponse(responseCode = "200", description = "Wallet found")
    @ApiResponse(responseCode = "404", description = "Wallet not found")
    @GetMapping
    public ResponseEntity<WalletResponse> findByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(walletMapper.toResponse(walletService.findByUserId(userId)));
    }

    @Operation(summary = "Update wallet balance", description = "Updates the balance for a specific user's wallet")
    @ApiResponse(responseCode = "200", description = "Wallet updated successfully")
    @ApiResponse(responseCode = "404", description = "Wallet not found")
    @ApiResponse(responseCode = "400", description = "Balance cannot be negative")
    @PutMapping
    public ResponseEntity<WalletResponse> updateBalance(@PathVariable Long userId, @Valid @RequestBody WalletRequest request) {
        var wallet = walletService.updateBalance(userId, request.balance());
        return ResponseEntity.ok(walletMapper.toResponse(wallet));
    }
}
