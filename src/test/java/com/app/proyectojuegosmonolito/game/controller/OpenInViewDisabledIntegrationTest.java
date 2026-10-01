package com.app.proyectojuegosmonolito.game.controller;

import com.app.proyectojuegosmonolito.TestcontainersConfiguration;
import com.app.proyectojuegosmonolito.game.model.GameImage;
import com.app.proyectojuegosmonolito.game.repository.GameRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static com.app.proyectojuegosmonolito.game.GameFixtures.game;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Guarda permanente de que {@code spring.jpa.open-in-view} esta realmente en false.
 *
 * <p>El resto de los tests de controlador son {@code @Transactional}, asi que la sesion
 * de JPA sigue abierta a traves del {@code Page.map()} del mapper y NUNCA ven un
 * {@code LazyInitializationException}. Esta clase NO lleva {@code @Transactional}: el
 * unico modo de que la suite detecte una regresion de open-in-view es con una peticion
 * real fuera de transaccion.
 *
 * <p>Ver {@code GameMapper.toResponse}, que desreferencia {@code Game.gallery}.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
class OpenInViewDisabledIntegrationTest {

    @Value("${spring.jpa.open-in-view}")
    private boolean openInView;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameRepository gameRepository;

    private String probeName;
    private String probeGalleryUrl;

    @BeforeEach
    void seedGameWithGallery() {
        probeName = "probe" + UUID.randomUUID().toString().substring(0, 8);
        probeGalleryUrl = "https://pub-test.r2.dev/" + probeName + "/gallery/1.jpg";

        var fixture = game();
        fixture.setName(probeName);
        var persisted = gameRepository.save(fixture);
        persisted.getGallery().add(GameImage.builder()
                .game(persisted)
                .url(probeGalleryUrl)
                .position(0)
                .createdAt(Instant.now())
                .build());
        gameRepository.save(persisted);
    }

    @AfterEach
    void cleanUp() {
        gameRepository.findByName(probeName).ifPresent(gameRepository::delete);
    }

    @Test
    void openInViewIsDisabled() {
        assertThat(openInView)
                .as("spring.jpa.open-in-view debe quedar bajo spring:, no bajo app: (ver application.yaml)")
                .isFalse();
    }

    @Test
    void getGamesReturnsGalleryWithoutOpenSession() throws Exception {
        mockMvc.perform(get("/api/v1/games").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.name == '" + probeName + "')].galleryUrls[0]")
                        .value(probeGalleryUrl));
    }

    @Test
    void getGameByIdReturnsGalleryWithoutOpenSession() throws Exception {
        var id = gameRepository.findByName(probeName).orElseThrow().getId();
        mockMvc.perform(get("/api/v1/games/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.galleryUrls[0]").value(probeGalleryUrl));
    }
}
