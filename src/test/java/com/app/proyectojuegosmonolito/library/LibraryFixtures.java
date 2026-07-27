package com.app.proyectojuegosmonolito.library;

import com.app.proyectojuegosmonolito.game.model.Game;
import com.app.proyectojuegosmonolito.library.model.Library;
import com.app.proyectojuegosmonolito.user.model.User;

import java.time.Instant;

public class LibraryFixtures {

    public static Library library(User user, Game game) {
        return Library.builder()
                .user(user)
                .game(game)
                .acquiredAt(Instant.now())
                .build();
    }

    public static Library library(Long id, User user, Game game) {
        return Library.builder()
                .id(id)
                .user(user)
                .game(game)
                .acquiredAt(Instant.now())
                .build();
    }
}
