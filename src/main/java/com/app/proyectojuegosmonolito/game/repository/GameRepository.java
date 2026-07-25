package com.app.proyectojuegosmonolito.game.repository;

import com.app.proyectojuegosmonolito.game.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {
}
