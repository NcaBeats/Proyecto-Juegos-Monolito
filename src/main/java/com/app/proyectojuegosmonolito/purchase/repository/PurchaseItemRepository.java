package com.app.proyectojuegosmonolito.purchase.repository;

import com.app.proyectojuegosmonolito.purchase.model.PurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, Long> {
}
