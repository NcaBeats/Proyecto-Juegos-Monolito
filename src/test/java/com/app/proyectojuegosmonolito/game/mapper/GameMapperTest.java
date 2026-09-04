package com.app.proyectojuegosmonolito.game.mapper;

import com.app.proyectojuegosmonolito.game.dto.GameRequest;
import com.app.proyectojuegosmonolito.game.dto.GameResponse;
import com.app.proyectojuegosmonolito.game.model.Game;
import com.app.proyectojuegosmonolito.game.model.GameState;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class GameMapperTest {

    private final GameMapper mapper = new GameMapper();

    @Test
    void toEntity_shouldMapAllFields() {
        var request = new GameRequest("Test Game", new BigDecimal("29.99"), 20,
                "Description", GameState.AVAILABLE, LocalDate.of(2026, 6, 15),
                List.of("Action", "RPG"), "min specs", "rec specs");

        var result = mapper.toEntity(request);

        assertThat(result.getName()).isEqualTo("Test Game");
        assertThat(result.getOriginalPrice()).isEqualByComparingTo("29.99");
        assertThat(result.getDiscountPercent()).isEqualTo(20);
        assertThat(result.getDescription()).isEqualTo("Description");
        assertThat(result.getState()).isEqualTo(GameState.AVAILABLE);
        assertThat(result.getLaunchDate()).isEqualTo(LocalDate.of(2026, 6, 15));
        assertThat(result.getMinimumSpecs()).isEqualTo("min specs");
        assertThat(result.getRecommendedSpecs()).isEqualTo("rec specs");
        assertThat(result.getId()).isNull();
        assertThat(result.getCreatedAt()).isNull();
    }

    @Test
    void toResponse_shouldMapAllFields() {
        var createdAt = Instant.parse("2026-01-01T00:00:00Z");
        var game = Game.builder()
                .id(1L).name("Test Game").originalPrice(new BigDecimal("29.99"))
                .discountPercent(25)
                .description("Description").state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2026, 6, 15)).createdAt(createdAt)
                .minimumSpecs("min specs").recommendedSpecs("rec specs")
                .categories(new ArrayList<>())
                .build();

        var result = mapper.toResponse(game);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Test Game");
        assertThat(result.originalPrice()).isEqualByComparingTo("29.99");
        assertThat(result.price()).isEqualByComparingTo("22.49");
        assertThat(result.discountPercent()).isEqualTo(25);
        assertThat(result.description()).isEqualTo("Description");
        assertThat(result.state()).isEqualTo(GameState.AVAILABLE);
        assertThat(result.launchDate()).isEqualTo(LocalDate.of(2026, 6, 15));
        assertThat(result.categories()).isEmpty();
        assertThat(result.minimumSpecs()).isEqualTo("min specs");
        assertThat(result.recommendedSpecs()).isEqualTo("rec specs");
        assertThat(result.createdAt()).isEqualTo(createdAt);
    }
}
