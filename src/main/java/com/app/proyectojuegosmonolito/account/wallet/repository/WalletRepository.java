package com.app.proyectojuegosmonolito.account.wallet.repository;

import com.app.proyectojuegosmonolito.account.wallet.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
}
