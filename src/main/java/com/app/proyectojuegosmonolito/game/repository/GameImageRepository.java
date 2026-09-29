package com.app.proyectojuegosmonolito.game.repository;

import com.app.proyectojuegosmonolito.game.model.GameImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameImageRepository extends JpaRepository<GameImage, Long> {
    List<GameImage> findByGame_IdOrderByPositionAsc(Long gameId);
    void deleteByGame_Id(Long gameId);
}
