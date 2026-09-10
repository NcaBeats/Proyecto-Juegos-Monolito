package com.app.proyectojuegosmonolito.game.controller;

import com.app.proyectojuegosmonolito.TestcontainersConfiguration;
import com.app.proyectojuegosmonolito.account.user.model.Role;
import com.app.proyectojuegosmonolito.account.user.model.User;
import com.app.proyectojuegosmonolito.account.user.service.UserService;
import com.app.proyectojuegosmonolito.game.dto.GameRequest;
import com.app.proyectojuegosmonolito.game.model.GameState;
import com.app.proyectojuegosmonolito.game.repository.GameRepository;
import com.app.proyectojuegosmonolito.game.service.ImageService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.app.proyectojuegosmonolito.game.GameFixtures.*;
import static com.app.proyectojuegosmonolito.account.user.UserFixtures.profile;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@Transactional
class GameControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private UserService userService;

    @MockitoBean
    private ImageService imageService;

    private User createAdmin() {
        var admin = User.builder()
                .email("gameadmin@test.com")
                .password("pass123")
                .role(Role.ADMIN)
                .createdAt(java.time.Instant.now())
                .build();
        return userService.create(admin, profile(admin));
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        var saved = gameRepository.save(game());

        mockMvc.perform(get("/api/v1/games/{id}", saved.getId()).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("game"));
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/games/{id}", 999L).with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAll_shouldReturnPage() throws Exception {
        gameRepository.saveAll(List.of(game("Alpha", BigDecimal.TEN), game("Beta", BigDecimal.TEN), game("Gamma", BigDecimal.TEN)));

        mockMvc.perform(get("/api/v1/games")
                        .with(jwt())
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(3));
    }

    @Test
    void findDiscounted_shouldReturn200() throws Exception {
        gameRepository.save(gameWithDiscount("Discounted Game", BigDecimal.TEN, 50));
        gameRepository.save(game("Full Price Game", BigDecimal.TEN));

        mockMvc.perform(get("/api/v1/games/discounted")
                        .with(jwt())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Discounted Game"))
                .andExpect(jsonPath("$.content[0].discountPercent").value(50));
    }

    @Test
    void create_shouldReturn201() throws Exception {
        when(imageService.store(any(MultipartFile.class), anyString())).thenReturn("https://example.com/img.png");
        var admin = createAdmin();
        var metadata = new MockMultipartFile("metadata", null, MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(new GameRequest("Nuevo Juego", new BigDecimal("29.99"), 10, "Descripción",
                        GameState.AVAILABLE, LocalDate.of(2026, 12, 1), List.of("Action"), null, null, null, null)));
        var image = new MockMultipartFile("image", "cover.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/v1/games")
                        .file(metadata)
                        .file(image)
                        .with(jwt().jwt(b -> b.subject(admin.getId().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Nuevo Juego"))
                .andExpect(jsonPath("$.discountPercent").value(10))
                .andExpect(jsonPath("$.originalPrice").value(29.99))
                .andExpect(jsonPath("$.price").value(26.99));
    }

    @Test
    void create_withInvalidBody_shouldReturn400() throws Exception {
        var metadata = new MockMultipartFile("metadata", null, MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(new GameRequest(null, null, null, null, null, null, null, null, null, null, null)));

        mockMvc.perform(multipart("/api/v1/games")
                        .file(metadata)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.errors.length()").value(8));
    }

    @Test
    void update_shouldReturn200() throws Exception {
        var saved = gameRepository.save(game());
        var admin = createAdmin();
        var metadata = new MockMultipartFile("metadata", null, MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(new GameRequest("Actualizado", new BigDecimal("49.99"), 20, "Nueva desc",
                        GameState.COMING_SOON, LocalDate.of(2027, 1, 1), List.of("Action"), null, null, null, null)));

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/games/{id}", saved.getId())
                        .file(metadata)
                        .with(jwt().jwt(b -> b.subject(admin.getId().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Actualizado"));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        var saved = gameRepository.save(game());

        mockMvc.perform(delete("/api/v1/games/{id}", saved.getId())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/v1/games/{id}", 999L)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_withNonNumericId_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/games/{id}", "abc").with(jwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"));
    }

    @Test
    void nonexistentRoute_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/nonexistent").with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"));
    }
}
