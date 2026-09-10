package com.app.proyectojuegosmonolito.game.repository;

import com.app.proyectojuegosmonolito.game.model.Game;
import com.app.proyectojuegosmonolito.game.model.GameState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {
    Page<Game> findByDiscountPercentGreaterThan(Integer discountPercent, Pageable pageable);
    Page<Game> findByBannerUrlIsNotNull(Pageable pageable);
    Page<Game> findByCategories_Name(String categoryName, Pageable pageable);
    Page<Game> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Optional<Game> findByName(String name);
    Page<Game> findBySeller_Id(Long sellerId, Pageable pageable);
    Page<Game> findBySeller_IdAndNameContainingIgnoreCase(Long sellerId, String name, Pageable pageable);
    boolean existsByIdAndSeller_Id(Long id, Long sellerId);
    long countByState(GameState state);

    @Query("""
            SELECT COALESCE(SUM(g.originalPrice * (100 - g.discountPercent) / 100), 0)
            FROM Game g
            WHERE g.state = :state
            """)
    BigDecimal sumDiscountedPriceByState(@Param("state") GameState state);
}
