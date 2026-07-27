package com.app.proyectojuegosmonolito.user.controller;

import com.app.proyectojuegosmonolito.TestcontainersConfiguration;
import com.app.proyectojuegosmonolito.user.service.UserService;
import com.app.proyectojuegosmonolito.user.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static com.app.proyectojuegosmonolito.user.UserFixtures.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@Transactional
class WalletControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private WalletService walletService;

    @Test
    void getByUserId_shouldReturn200() throws Exception {
        var user = userService.create(user());

        mockMvc.perform(get("/api/v1/users/{userId}/wallet", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    void getByUserId_whenNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/users/999/wallet"))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn200() throws Exception {
        var user = userService.create(user());

        mockMvc.perform(put("/api/v1/users/{userId}/wallet", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"balance\": 50.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(50.00));
    }

    @Test
    void update_withNegativeBalance_shouldReturn400() throws Exception {
        var user = userService.create(user());

        mockMvc.perform(put("/api/v1/users/{userId}/wallet", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"balance\": -10}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_whenNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(put("/api/v1/users/999/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"balance\": 50.00}"))
                .andExpect(status().isNotFound());
    }
}
