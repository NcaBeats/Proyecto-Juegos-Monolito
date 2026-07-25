package com.app.proyectojuegosmonolito.user.repository;

import com.app.proyectojuegosmonolito.user.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
}
