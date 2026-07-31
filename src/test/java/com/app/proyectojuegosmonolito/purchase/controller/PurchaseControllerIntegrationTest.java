package com.app.proyectojuegosmonolito.purchase.controller;

import com.app.proyectojuegosmonolito.TestcontainersConfiguration;
import com.app.proyectojuegosmonolito.game.repository.GameRepository;
import com.app.proyectojuegosmonolito.purchase.dto.PurchaseItemRequest;
import com.app.proyectojuegosmonolito.purchase.dto.PurchaseRequest;
import com.app.proyectojuegosmonolito.account.user.service.UserService;
import com.app.proyectojuegosmonolito.account.wallet.service.WalletService;
import tools.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

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
class PurchaseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
        var token = jwt().jwt(b -> b.subject(user.getId().toString()));

        mockMvc.perform(post("/api/v1/purchases").with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PurchaseRequest(List.of(new PurchaseItemRequest(game.getId(), 1))))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.totalAmount").value(29.99));
    }

    @Test
    void create_withInvalidBody_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/purchases").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PurchaseRequest(List.of()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Invalid request content."))
                .andExpect(jsonPath("$.errors[0]").value(
                    containsString("items")));
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        var user = userService.create(user());
        walletService.updateBalance(user.getId(), new BigDecimal("100.00"));
        var game = gameRepository.save(game());
        var token = jwt().jwt(b -> b.subject(user.getId().toString()));

        var response = mockMvc.perform(post("/api/v1/purchases").with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PurchaseRequest(List.of(new PurchaseItemRequest(game.getId(), 1))))))
                .andExpect(status().isCreated())
                .andReturn();
        var id = JsonPath.parse(response.getResponse().getContentAsString()).read("$.id", Long.class);

        mockMvc.perform(get("/api/v1/purchases/{id}", id).with(token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/purchases/999").with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMyPurchases_shouldReturn200() throws Exception {
        var user = userService.create(user());
        walletService.updateBalance(user.getId(), new BigDecimal("100.00"));
        var game = gameRepository.save(game());
        var token = jwt().jwt(b -> b.subject(user.getId().toString()));

        mockMvc.perform(post("/api/v1/purchases").with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PurchaseRequest(List.of(new PurchaseItemRequest(game.getId(), 1))))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/purchases").with(token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }
}
