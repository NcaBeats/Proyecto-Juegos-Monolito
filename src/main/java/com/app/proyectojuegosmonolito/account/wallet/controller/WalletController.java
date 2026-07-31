package com.app.proyectojuegosmonolito.account.wallet.controller;

import com.app.proyectojuegosmonolito.security.service.SecurityContext;
import com.app.proyectojuegosmonolito.account.wallet.dto.WalletDepositRequest;
import com.app.proyectojuegosmonolito.account.wallet.dto.WalletRequest;
import com.app.proyectojuegosmonolito.account.wallet.dto.WalletResponse;
import com.app.proyectojuegosmonolito.account.wallet.mapper.WalletMapper;
import com.app.proyectojuegosmonolito.account.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Wallet", description = "Wallet management APIs")
@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final WalletMapper walletMapper;
    private final SecurityContext securityContext;

    @Operation(summary = "Get wallet", description = "Returns the wallet for the authenticated user")
    @ApiResponse(responseCode = "200", description = "Wallet found")
    @ApiResponse(responseCode = "404", description = "Wallet not found")
    @GetMapping
    public ResponseEntity<WalletResponse> findMyWallet() {
        var userId = securityContext.getCurrentUserId();
        return ResponseEntity.ok(walletMapper.toResponse(walletService.findByUserId(userId)));
    }

    @Operation(summary = "Update wallet balance", description = "Updates the balance for the authenticated user's wallet")
    @ApiResponse(responseCode = "200", description = "Wallet updated successfully")
    @ApiResponse(responseCode = "404", description = "Wallet not found")
    @ApiResponse(responseCode = "400", description = "Balance cannot be negative")
    @PutMapping
    public ResponseEntity<WalletResponse> updateMyBalance(@Valid @RequestBody WalletRequest request) {
        var userId = securityContext.getCurrentUserId();
        var wallet = walletService.updateBalance(userId, request.balance());
        return ResponseEntity.ok(walletMapper.toResponse(wallet));
    }

    @Operation(summary = "Deposit to wallet", description = "Adds funds to the authenticated user's wallet")
    @ApiResponse(responseCode = "200", description = "Deposit successful")
    @ApiResponse(responseCode = "400", description = "Amount must be positive")
    @PostMapping("/deposit")
    public ResponseEntity<WalletResponse> deposit(@Valid @RequestBody WalletDepositRequest request) {
        var userId = securityContext.getCurrentUserId();
        var wallet = walletService.deposit(userId, request.amount());
        return ResponseEntity.ok(walletMapper.toResponse(wallet));
    }
}
