package com.app.proyectojuegosmonolito.game;

import com.app.proyectojuegosmonolito.game.model.Game;
import com.app.proyectojuegosmonolito.game.model.GameState;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;

public class GameFixtures {

    public static Game game() {
        return Game.builder()
                .name("game")
                .originalPrice(BigDecimal.TEN)
                .discountPercent(0)
                .description("description")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.now())
                .createdAt(Instant.now())
                .categories(new ArrayList<>())
                .build();
    }

    public static Game game(Long id) {
        return Game.builder()
                .id(id)
                .name("game" + id)
                .originalPrice(BigDecimal.TEN)
                .discountPercent(0)
                .description("description")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.now())
                .createdAt(Instant.now())
                .categories(new ArrayList<>())
                .build();
    }

    public static Game game(String name, BigDecimal originalPrice) {
        return Game.builder()
                .name(name)
                .originalPrice(originalPrice)
                .discountPercent(0)
                .description("description")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.now())
                .createdAt(Instant.now())
                .categories(new ArrayList<>())
                .build();
    }

    public static Game game(Long id, String name, BigDecimal originalPrice) {
        return Game.builder()
                .id(id)
                .name(name)
                .originalPrice(originalPrice)
                .discountPercent(0)
                .description("description")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.now())
                .createdAt(Instant.now())
                .categories(new ArrayList<>())
                .build();
    }

    public static Game gameWithDiscount(String name, BigDecimal originalPrice, Integer discountPercent) {
        return Game.builder()
                .name(name)
                .originalPrice(originalPrice)
                .discountPercent(discountPercent)
                .description("description")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.now())
                .createdAt(Instant.now())
                .categories(new ArrayList<>())
                .build();
    }
}
