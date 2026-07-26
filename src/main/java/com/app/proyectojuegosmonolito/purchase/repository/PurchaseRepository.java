package com.app.proyectojuegosmonolito.purchase.repository;

import com.app.proyectojuegosmonolito.purchase.model.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    Page<Purchase> findByUser_Id(Long userId, Pageable pageable);
}
