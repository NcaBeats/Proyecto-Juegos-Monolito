package com.app.proyectojuegosmonolito.purchase.controller;

import com.app.proyectojuegosmonolito.TestcontainersConfiguration;
import com.app.proyectojuegosmonolito.game.repository.GameRepository;
import com.app.proyectojuegosmonolito.user.service.UserService;
import com.app.proyectojuegosmonolito.user.service.WalletService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static com.app.proyectojuegosmonolito.game.GameFixtures.*;
import static com.app.proyectojuegosmonolito.user.UserFixtures.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@Transactional
class PurchaseControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private GameRepository gameRepository;

    @Test
    void create_shouldReturn201() throws Exception {
        var user = userService.create(user());
        walletService.updateBalance(user.getId(), new BigDecimal("100.00"));
        var game = gameRepository.save(game("Test Game", new BigDecimal("29.99")));

        mockMvc.perform(post("/api/v1/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": %d,
                                    "items": [{"gameId": %d, "quantity": 1}]
                                }
                                """.formatted(user.getId(), game.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.totalAmount").value(29.99));
    }

    @Test
    void create_withInvalidBody_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        var user = userService.create(user());
        walletService.updateBalance(user.getId(), new BigDecimal("100.00"));
        var game = gameRepository.save(game());
        var response = mockMvc.perform(post("/api/v1/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId": %d, "items": [{"gameId": %d, "quantity": 1}]}
                                """.formatted(user.getId(), game.getId())))
                .andExpect(status().isCreated())
                .andReturn();
        var id = JsonPath.parse(response.getResponse().getContentAsString()).read("$.id", Long.class);

        mockMvc.perform(get("/api/v1/purchases/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/purchases/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAll_shouldReturn200() throws Exception {
        var user = userService.create(user());
        walletService.updateBalance(user.getId(), new BigDecimal("100.00"));
        var game = gameRepository.save(game());
        mockMvc.perform(post("/api/v1/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId": %d, "items": [{"gameId": %d, "quantity": 1}]}
                                """.formatted(user.getId(), game.getId())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/purchases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getByUserId_shouldReturn200() throws Exception {
        var user = userService.create(user());
        walletService.updateBalance(user.getId(), new BigDecimal("100.00"));
        var game = gameRepository.save(game());
        mockMvc.perform(post("/api/v1/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId": %d, "items": [{"gameId": %d, "quantity": 1}]}
                                """.formatted(user.getId(), game.getId())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/purchases/user/{userId}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
