package com.app.proyectojuegosmonolito.library.mapper;

import com.app.proyectojuegosmonolito.game.model.Game;
import com.app.proyectojuegosmonolito.library.dto.LibraryResponse;
import com.app.proyectojuegosmonolito.library.model.Library;
import com.app.proyectojuegosmonolito.account.user.model.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class LibraryMapperTest {

    private final LibraryMapper mapper = new LibraryMapper();

    @Test
    void toResponse_shouldMapAllFields() {
        var acquiredAt = Instant.parse("2026-01-01T00:00:00Z");
        var user = User.builder().id(1L).build();
        var game = Game.builder().id(10L).build();
        var library = Library.builder()
                .id(1L).user(user).game(game).acquiredAt(acquiredAt)
                .build();

        var result = mapper.toResponse(library);

        assertThat(result).isEqualTo(new LibraryResponse(1L, 1L, 10L, acquiredAt));
    }
}
