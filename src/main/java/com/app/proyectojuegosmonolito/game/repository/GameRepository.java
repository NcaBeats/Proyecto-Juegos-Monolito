package com.app.proyectojuegosmonolito.game.repository;

import com.app.proyectojuegosmonolito.game.model.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {
    Page<Game> findByDiscountPercentGreaterThan(Integer discountPercent, Pageable pageable);
    Page<Game> findByBannerUrlIsNotNull(Pageable pageable);
}
