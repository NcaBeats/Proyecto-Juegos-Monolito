package com.app.proyectojuegosmonolito.game.mapper;

import com.app.proyectojuegosmonolito.game.dto.GameRequest;
import com.app.proyectojuegosmonolito.game.dto.GameResponse;
import com.app.proyectojuegosmonolito.game.model.Game;
import com.app.proyectojuegosmonolito.game.model.GameState;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class GameMapperTest {

    private final GameMapper mapper = new GameMapper();

    @Test
    void toEntity_shouldMapAllFields() {
        var request = new GameRequest("Test Game", new BigDecimal("29.99"),
                "Description", GameState.AVAILABLE, LocalDate.of(2026, 6, 15));

        var result = mapper.toEntity(request);

        assertThat(result.getName()).isEqualTo("Test Game");
        assertThat(result.getPrice()).isEqualByComparingTo("29.99");
        assertThat(result.getDescription()).isEqualTo("Description");
        assertThat(result.getState()).isEqualTo(GameState.AVAILABLE);
        assertThat(result.getLaunchDate()).isEqualTo(LocalDate.of(2026, 6, 15));
        assertThat(result.getId()).isNull();
        assertThat(result.getCreatedAt()).isNull();
    }

    @Test
    void toResponse_shouldMapAllFields() {
        var createdAt = Instant.parse("2026-01-01T00:00:00Z");
        var game = Game.builder()
                .id(1L).name("Test Game").price(new BigDecimal("29.99"))
                .description("Description").state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2026, 6, 15)).createdAt(createdAt)
                .build();

        var result = mapper.toResponse(game);

        assertThat(result).isEqualTo(new GameResponse(1L, "Test Game",
                new BigDecimal("29.99"), "Description", GameState.AVAILABLE,
                LocalDate.of(2026, 6, 15), createdAt));
    }
}
