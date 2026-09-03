package com.app.proyectojuegosmonolito.purchase.repository;

import com.app.proyectojuegosmonolito.purchase.model.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    @Override
    @EntityGraph(attributePaths = {"user", "items", "items.game"})
    Optional<Purchase> findById(Long id);

    @EntityGraph(attributePaths = {"user", "items", "items.game"})
    Page<Purchase> findByUser_Id(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "items", "items.game"})
    Optional<Purchase> findByUser_IdAndIdempotencyKey(Long userId, String idempotencyKey);

    @Transactional
    void deleteByUser_Id(Long userId);
}
