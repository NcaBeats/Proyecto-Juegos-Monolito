package com.app.proyectojuegosmonolito.purchase.repository;

import com.app.proyectojuegosmonolito.purchase.model.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    @Override
    @EntityGraph(attributePaths = {"user", "items", "items.game"})
    Optional<Purchase> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"user", "items", "items.game"})
    Page<Purchase> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "items", "items.game"})
    Page<Purchase> findByUser_Id(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "items", "items.game"})
    @Query("""
            SELECT p FROM Purchase p
            WHERE p.id IN (
                SELECT DISTINCT pi.purchase.id FROM PurchaseItem pi WHERE pi.game.seller.id = :sellerId
            )
            ORDER BY p.purchasedAt DESC, p.id DESC
            """)
    Page<Purchase> findBySeller_Id(@Param("sellerId") Long sellerId, Pageable pageable);

    @Query("""
            SELECT COUNT(pi) > 0 FROM PurchaseItem pi
            WHERE pi.purchase.id = :purchaseId AND pi.game.seller.id = :sellerId
            """)
    boolean existsByPurchaseIdAndGameSellerId(@Param("purchaseId") Long purchaseId, @Param("sellerId") Long sellerId);

    @EntityGraph(attributePaths = {"user", "items", "items.game"})
    Optional<Purchase> findByUser_IdAndIdempotencyKey(Long userId, String idempotencyKey);

    @Transactional
    void deleteByUser_Id(Long userId);
}
