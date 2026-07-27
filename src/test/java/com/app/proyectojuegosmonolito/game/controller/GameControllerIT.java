package com.app.proyectojuegosmonolito.game.controller;

import com.app.proyectojuegosmonolito.TestcontainersConfiguration;
import com.app.proyectojuegosmonolito.game.repository.GameRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static com.app.proyectojuegosmonolito.game.GameFixtures.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@Transactional
class GameControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameRepository gameRepository;

    @Test
    void getById_shouldReturn200() throws Exception {
        var saved = gameRepository.save(game());

        mockMvc.perform(get("/api/v1/games/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("game"));
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/games/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAll_shouldReturnPage() throws Exception {
        gameRepository.saveAll(List.of(game("Alpha", BigDecimal.TEN), game("Beta", BigDecimal.TEN), game("Gamma", BigDecimal.TEN)));

        mockMvc.perform(get("/api/v1/games")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void create_shouldReturn201() throws Exception {
        var request = """
                {
                    "name": "Nuevo Juego",
                    "price": 29.99,
                    "description": "Descripción",
                    "state": "AVAILABLE",
                    "launchDate": "2026-12-01"
                }
                """;

        mockMvc.perform(post("/api/v1/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Nuevo Juego"));
    }

    @Test
    void create_withInvalidBody_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturn200() throws Exception {
        var saved = gameRepository.save(game());
        var request = """
                {
                    "name": "Actualizado",
                    "price": 49.99,
                    "description": "Nueva desc",
                    "state": "COMING_SOON",
                    "launchDate": "2027-01-01"
                }
                """;

        mockMvc.perform(put("/api/v1/games/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Actualizado"));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        var saved = gameRepository.save(game());

        mockMvc.perform(delete("/api/v1/games/{id}", saved.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/v1/games/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}
