package com.app.proyectojuegosmonolito.config;

import com.app.proyectojuegosmonolito.TestcontainersConfiguration;
import com.app.proyectojuegosmonolito.game.model.Game;
import com.app.proyectojuegosmonolito.game.model.GameImage;
import com.app.proyectojuegosmonolito.game.model.GameState;
import com.app.proyectojuegosmonolito.game.repository.GameRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.*;

/**
 * El runner reescribe filas que el servicio ya no puede producir, asi que la
 * fixture las siembra por el repositorio: pasar por el servicio las rechazaria.
 * Con esto se prueba que el seguro funciona y que no toca lo que ya estaba bien.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = "app.media.normalize.enabled=true")
@Transactional
class MediaUrlNormalizerIntegrationTest {

    @Autowired
    private MediaUrlNormalizer normalizer;

    @Autowired
    private GameRepository gameRepository;

    @Test
    void run_shouldRewriteLegacyTrailerUrl() {
        var game = game("Half-Life 3");
        game.setVideoUrl("/uploads/games/half-life-3/trailer.mp4");
        gameRepository.saveAndFlush(game);

        normalizer.run();

        assertThat(gameRepository.findById(game.getId()).orElseThrow().getVideoUrl())
                .isEqualTo("https://pub-test.r2.dev/half-life-3/trailer.mp4");
    }

    @Test
    void run_shouldRewriteEveryLegacyMediaColumn() {
        var game = game("Half-Life 3");
        game.setVideoUrl("/uploads/games/half-life-3/trailer.mp4");
        game.setImageUrl("/uploads/games/half-life-3/card/portada.jpg");
        game.setBannerUrl("/uploads/games/half-life-3/banner/banner.jpg");
        game.getGallery().add(GameImage.builder()
                .game(game)
                .url("/uploads/games/half-life-3/gallery/1.jpg")
                .position(0)
                .createdAt(Instant.now())
                .build());
        gameRepository.saveAndFlush(game);

        normalizer.run();
        gameRepository.flush();

        var reloaded = gameRepository.findById(game.getId()).orElseThrow();
        var base = "https://pub-test.r2.dev/half-life-3";
        assertThat(reloaded.getVideoUrl()).isEqualTo(base + "/trailer.mp4");
        assertThat(reloaded.getImageUrl()).isEqualTo(base + "/card/portada.jpg");
        assertThat(reloaded.getBannerUrl()).isEqualTo(base + "/banner/banner.jpg");
        assertThat(reloaded.getGallery()).hasSize(1);
        assertThat(reloaded.getGallery().get(0).getUrl()).isEqualTo(base + "/gallery/1.jpg");
    }

    /** Idempotente: correrlo dos veces no debe alterar una URL ya absoluta. */
    @Test
    void run_twice_shouldBeIdempotent() {
        var game = game("Half-Life 3");
        game.setVideoUrl("/uploads/games/half-life-3/trailer.mp4");
        gameRepository.saveAndFlush(game);

        normalizer.run();
        normalizer.run();

        assertThat(gameRepository.findById(game.getId()).orElseThrow().getVideoUrl())
                .isEqualTo("https://pub-test.r2.dev/half-life-3/trailer.mp4");
    }

    /** Lo que ya cumple la invariante no se toca, en particular URLs de otros CDN. */
    @Test
    void run_shouldLeaveAbsoluteUrlsUntouched() {
        var game = game("Half-Life 3");
        game.setVideoUrl("https://pub-test.r2.dev/half-life-3/trailer.mp4");
        game.setImageUrl("https://imagenes-ejemplo.com/half-life-3/card.jpg");
        gameRepository.saveAndFlush(game);

        normalizer.run();

        var reloaded = gameRepository.findById(game.getId()).orElseThrow();
        assertThat(reloaded.getVideoUrl()).isEqualTo("https://pub-test.r2.dev/half-life-3/trailer.mp4");
        assertThat(reloaded.getImageUrl()).isEqualTo("https://imagenes-ejemplo.com/half-life-3/card.jpg");
    }

    private Game game(String name) {
        return Game.builder()
                .name(name)
                .originalPrice(BigDecimal.TEN)
                .discountPercent(0)
                .description("description")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.now())
                .createdAt(Instant.now())
                .categories(new ArrayList<>())
                .build();
    }
}