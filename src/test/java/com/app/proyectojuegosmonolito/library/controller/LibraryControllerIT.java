package com.app.proyectojuegosmonolito.library.controller;

import com.app.proyectojuegosmonolito.TestcontainersConfiguration;
import com.app.proyectojuegosmonolito.game.repository.GameRepository;
import com.app.proyectojuegosmonolito.library.service.LibraryService;
import com.app.proyectojuegosmonolito.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static com.app.proyectojuegosmonolito.game.GameFixtures.*;
import static com.app.proyectojuegosmonolito.user.UserFixtures.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@Transactional
class LibraryControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private LibraryService libraryService;

    @Test
    void add_shouldReturn201() throws Exception {
        var user = userRepository.save(user());
        var game = gameRepository.save(game());

        mockMvc.perform(post("/api/v1/users/{userId}/library", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gameId\": " + game.getId() + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.gameId").value(game.getId()));
    }

    @Test
    void add_withDuplicate_shouldReturn400() throws Exception {
        var user = userRepository.save(user());
        var game = gameRepository.save(game());
        libraryService.add(user.getId(), game.getId());

        mockMvc.perform(post("/api/v1/users/{userId}/library", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gameId\": " + game.getId() + "}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void add_withNonExistentUser_shouldReturn404() throws Exception {
        mockMvc.perform(post("/api/v1/users/{userId}/library", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gameId\": 1}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void add_withNonExistentGame_shouldReturn404() throws Exception {
        var user = userRepository.save(user());

        mockMvc.perform(post("/api/v1/users/{userId}/library", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gameId\": 999}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void add_withNullGameId_shouldReturn400() throws Exception {
        var user = userRepository.save(user());

        mockMvc.perform(post("/api/v1/users/{userId}/library", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getByUserId_shouldReturn200() throws Exception {
        var user = userRepository.save(user());
        var game = gameRepository.save(game());
        libraryService.add(user.getId(), game.getId());

        mockMvc.perform(get("/api/v1/users/{userId}/library", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void remove_shouldReturn204() throws Exception {
        var user = userRepository.save(user());
        var game = gameRepository.save(game());
        var lib = libraryService.add(user.getId(), game.getId());

        mockMvc.perform(delete("/api/v1/users/{userId}/library/{id}", user.getId(), lib.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void remove_whenNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/v1/users/{userId}/library/{id}", 1L, 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void remove_whenNotOwned_shouldReturn400() throws Exception {
        var user1 = userRepository.save(user("user1", "user1@test.com"));
        var user2 = userRepository.save(user("user2", "user2@test.com"));
        var game = gameRepository.save(game());
        var lib = libraryService.add(user1.getId(), game.getId());

        mockMvc.perform(delete("/api/v1/users/{userId}/library/{id}", user2.getId(), lib.getId()))
                .andExpect(status().isBadRequest());
    }
}
