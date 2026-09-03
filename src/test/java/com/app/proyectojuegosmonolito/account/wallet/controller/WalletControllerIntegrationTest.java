package com.app.proyectojuegosmonolito.account.wallet.controller;

import com.app.proyectojuegosmonolito.TestcontainersConfiguration;
import com.app.proyectojuegosmonolito.account.wallet.dto.WalletDepositRequest;
import com.app.proyectojuegosmonolito.account.wallet.dto.WalletRequest;
import com.app.proyectojuegosmonolito.account.user.model.Role;
import com.app.proyectojuegosmonolito.account.user.model.User;
import com.app.proyectojuegosmonolito.account.user.service.UserService;
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

import static com.app.proyectojuegosmonolito.account.user.UserFixtures.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@Transactional
class WalletControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Test
    void getMyWallet_shouldReturn200() throws Exception {
        var user = userService.create(user());
        var token = jwt().jwt(b -> b.subject(user.getId().toString()));

        mockMvc.perform(get("/api/v1/wallet").with(token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    void update_shouldReturn200() throws Exception {
        var admin = userService.create(User.builder()
                .email("admin@test.com")
                .password("pass123")
                .role(Role.ADMIN)
                .build());
        var token = jwt().jwt(b -> b.subject(admin.getId().toString()))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));

        mockMvc.perform(put("/api/v1/wallet").with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new WalletRequest(BigDecimal.valueOf(50)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(50.00));
    }

    @Test
    void update_withNegativeBalance_shouldReturn400() throws Exception {
        var admin = userService.create(User.builder()
                .email("admin2@test.com")
                .password("pass123")
                .role(Role.ADMIN)
                .build());
        var token = jwt().jwt(b -> b.subject(admin.getId().toString()))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));

        mockMvc.perform(put("/api/v1/wallet").with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new WalletRequest(BigDecimal.valueOf(-10)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Invalid request content."))
                .andExpect(jsonPath("$.errors[0]").value(
                    containsString("balance")));
    }

    @Test
    void update_withUserRole_shouldReturn403() throws Exception {
        var user = userService.create(user());
        var token = jwt().jwt(b -> b.subject(user.getId().toString()));

        mockMvc.perform(put("/api/v1/wallet").with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new WalletRequest(BigDecimal.valueOf(50)))))
                .andExpect(status().isForbidden());
    }

    @Test
    void deposit_shouldAddFunds() throws Exception {
        var user = userService.create(user());
        var token = jwt().jwt(b -> b.subject(user.getId().toString()));

        mockMvc.perform(post("/api/v1/wallet/deposit").with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new WalletDepositRequest(BigDecimal.valueOf(50)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(50.00));
    }

    @Test
    void deposit_withNegativeAmount_shouldReturn400() throws Exception {
        var user = userService.create(user());
        var token = jwt().jwt(b -> b.subject(user.getId().toString()));

        mockMvc.perform(post("/api/v1/wallet/deposit").with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new WalletDepositRequest(BigDecimal.valueOf(-10)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Invalid request content."))
                .andExpect(jsonPath("$.errors[0]").value(
                    containsString("amount")));
    }

    @Test
    void deposit_withZeroAmount_shouldReturn400() throws Exception {
        var user = userService.create(user());
        var token = jwt().jwt(b -> b.subject(user.getId().toString()));

        mockMvc.perform(post("/api/v1/wallet/deposit").with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new WalletDepositRequest(BigDecimal.ZERO))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Invalid request content."))
                .andExpect(jsonPath("$.errors[0]").value(
                    containsString("amount")));
    }
}
