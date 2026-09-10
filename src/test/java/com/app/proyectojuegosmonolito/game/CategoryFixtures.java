package com.app.proyectojuegosmonolito.game;

import com.app.proyectojuegosmonolito.game.model.Category;

import java.time.Instant;

public class CategoryFixtures {

    public static Category category() {
        return category("Action");
    }

    public static Category category(String name) {
        return Category.builder()
                .name(name)
                .createdAt(Instant.now())
                .build();
    }
}