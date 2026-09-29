package com.app.proyectojuegosmonolito.security.controller;

import com.app.proyectojuegosmonolito.TestcontainersConfiguration;
import com.app.proyectojuegosmonolito.security.dto.LoginRequest;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica el 429 de fuerza bruta sobre POST /api/v1/auth/login. Necesita su
 * propio contexto con limites bajos, porque el perfil test los relaja para que
 * el resto de la suite pueda hacer logins fallidos a proposito.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
        "app.security.login.max-per-email=3",
        "app.security.login.max-per-ip=1000"
})
class AuthControllerRateLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_afterTooManyFailures_shouldReturn429WithRetryAfter() throws Exception {
        // El limiter vive en memoria y @Transactional no lo revierte, asi que
        // cada test usa su propio correo para no heredar el bloqueo de otro.
        String body = objectMapper.writeValueAsString(
                new LoginRequest("ratelimit-a@gmail.com", "wrongpass"));

        // Dos fallos: todavia por debajo del limite de 3.
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnauthorized());
        }

        // El tercero agota la ventana.
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());

        // A partir de ahi la peticion se rechaza antes de tocar la base de datos.
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER));
    }

    @Test
    void login_withOtherEmail_shouldNotBeBlocked() throws Exception {
        String blocked = objectMapper.writeValueAsString(
                new LoginRequest("ratelimit-b@gmail.com", "wrongpass"));
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(blocked))
                    .andExpect(status().isUnauthorized());
        }

        // El limite por correo no debe arrastrar a otros usuarios.
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("ratelimit-c@gmail.com", "wrongpass"))))
                .andExpect(status().isUnauthorized());
    }
}
