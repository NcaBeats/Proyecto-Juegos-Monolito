package com.app.proyectojuegosmonolito.account.wallet.service;

import com.app.proyectojuegosmonolito.account.wallet.model.Wallet;
import com.app.proyectojuegosmonolito.account.wallet.repository.WalletRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    public Wallet findByUserId(Long userId) {
        log.info("Fetching wallet for user: {}", userId);
        return walletRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Wallet not found for user: {}", userId);
                    return new EntityNotFoundException("Wallet not found: " + userId);
                });
    }

    @Transactional
    public Wallet updateBalance(Long userId, BigDecimal newBalance) {
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Attempted to set negative balance for user {}: {}", userId, newBalance);
            throw new IllegalArgumentException("Balance cannot be negative");
        }
        log.info("Updating wallet for user {}: new balance={}", userId, newBalance);
        var wallet = findByUserId(userId);
        wallet.update(newBalance);
        var saved = walletRepository.save(wallet);
        log.info("Updated wallet for user {}: balance={}", userId, saved.getBalance());
        return saved;
    }

    @Transactional
    public Wallet deposit(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Attempted to deposit non-positive amount for user {}: {}", userId, amount);
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        log.info("Depositing {} to wallet for user {}", amount, userId);
        var wallet = findByUserId(userId);
        wallet.update(wallet.getBalance().add(amount));
        var saved = walletRepository.save(wallet);
        log.info("Deposited {} to wallet for user {}: new balance={}", amount, userId, saved.getBalance());
        return saved;
    }
}
