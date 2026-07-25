package com.app.proyectojuegosmonolito.user.service;

import com.app.proyectojuegosmonolito.user.Wallet;
import com.app.proyectojuegosmonolito.user.repository.WalletRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    public Wallet findByUserId(Long userId) {
        return walletRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Wallet not found: " + userId));
    }

    @Transactional
    public Wallet updateBalance(Long userId, BigDecimal newBalance) {
        var wallet = findByUserId(userId);
        wallet.update(newBalance);
        return walletRepository.save(wallet);
    }
}
