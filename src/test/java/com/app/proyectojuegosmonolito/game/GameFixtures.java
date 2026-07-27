package com.app.proyectojuegosmonolito.game;

import com.app.proyectojuegosmonolito.game.model.Game;
import com.app.proyectojuegosmonolito.game.model.GameState;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class GameFixtures {

    public static Game game() {
        return Game.builder()
                .name("game")
                .price(BigDecimal.TEN)
                .description("description")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.now())
                .createdAt(Instant.now())
                .build();
    }

    public static Game game(Long id) {
        return Game.builder()
                .id(id)
                .name("game" + id)
                .price(BigDecimal.TEN)
                .description("description")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.now())
                .createdAt(Instant.now())
                .build();
    }

    public static Game game(String name, BigDecimal price) {
        return Game.builder()
                .name(name)
                .price(price)
                .description("description")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.now())
                .createdAt(Instant.now())
                .build();
    }

    public static Game game(Long id, String name, BigDecimal price) {
        return Game.builder()
                .id(id)
                .name(name)
                .price(price)
                .description("description")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.now())
                .createdAt(Instant.now())
                .build();
    }
}
