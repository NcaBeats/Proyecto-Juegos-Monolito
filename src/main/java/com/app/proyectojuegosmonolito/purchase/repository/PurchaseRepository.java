package com.app.proyectojuegosmonolito.purchase.repository;

import com.app.proyectojuegosmonolito.purchase.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
}
