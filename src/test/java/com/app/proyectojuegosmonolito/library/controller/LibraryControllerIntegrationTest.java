package com.app.proyectojuegosmonolito.library.controller;

import com.app.proyectojuegosmonolito.TestcontainersConfiguration;
import com.app.proyectojuegosmonolito.game.repository.GameRepository;
import com.app.proyectojuegosmonolito.library.dto.LibraryRequest;
import com.app.proyectojuegosmonolito.library.service.LibraryService;
import com.app.proyectojuegosmonolito.account.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static com.app.proyectojuegosmonolito.game.GameFixtures.*;
import static com.app.proyectojuegosmonolito.account.user.UserFixtures.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@Transactional
class LibraryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
        var token = jwt().jwt(b -> b.subject(user.getId().toString()));

        mockMvc.perform(post("/api/v1/library").with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LibraryRequest(game.getId()))))
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
        var token = jwt().jwt(b -> b.subject(user.getId().toString()));

        mockMvc.perform(post("/api/v1/library").with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LibraryRequest(game.getId()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("already in library")));
    }

    @Test
    void add_withNonExistentGame_shouldReturn404() throws Exception {
        var user = userRepository.save(user());
        var token = jwt().jwt(b -> b.subject(user.getId().toString()));

        mockMvc.perform(post("/api/v1/library").with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LibraryRequest(999L))))
                .andExpect(status().isNotFound());
    }

    @Test
    void add_withNullGameId_shouldReturn400() throws Exception {
        var user = userRepository.save(user());
        var token = jwt().jwt(b -> b.subject(user.getId().toString()));

        mockMvc.perform(post("/api/v1/library").with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LibraryRequest(null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Invalid request content."))
                .andExpect(jsonPath("$.errors[0]").value(
                    containsString("gameId")));
    }

    @Test
    void getMyLibrary_shouldReturn200() throws Exception {
        var user = userRepository.save(user());
        var game = gameRepository.save(game());
        libraryService.add(user.getId(), game.getId());
        var token = jwt().jwt(b -> b.subject(user.getId().toString()));

        mockMvc.perform(get("/api/v1/library").with(token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void removeByGame_shouldReturn204() throws Exception {
        var user = userRepository.save(user());
        var game = gameRepository.save(game());
        libraryService.add(user.getId(), game.getId());
        var token = jwt().jwt(b -> b.subject(user.getId().toString()));

        mockMvc.perform(delete("/api/v1/library/game/{gameId}", game.getId()).with(token))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeByGame_whenNotFound_shouldReturn404() throws Exception {
        var token = jwt().jwt(b -> b.subject("1"));

        mockMvc.perform(delete("/api/v1/library/game/{gameId}", 999L).with(token))
                .andExpect(status().isNotFound());
    }

    @Test
    void clear_shouldReturn204() throws Exception {
        var user = userRepository.save(user());
        var game1 = gameRepository.save(game());
        var game2 = gameRepository.save(game("Other Game", BigDecimal.valueOf(5)));
        libraryService.add(user.getId(), game1.getId());
        libraryService.add(user.getId(), game2.getId());
        var token = jwt().jwt(b -> b.subject(user.getId().toString()));

        mockMvc.perform(delete("/api/v1/library").with(token))
                .andExpect(status().isNoContent());
    }
}
