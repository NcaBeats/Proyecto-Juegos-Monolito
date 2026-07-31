package com.app.proyectojuegosmonolito.account.user.controller;

import com.app.proyectojuegosmonolito.TestcontainersConfiguration;
import com.app.proyectojuegosmonolito.account.user.dto.UserRequestCreate;
import com.app.proyectojuegosmonolito.account.user.dto.UserUpdateRequest;
import com.app.proyectojuegosmonolito.account.user.repository.UserRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;

import static com.app.proyectojuegosmonolito.account.user.UserFixtures.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    void getById_shouldReturn200() throws Exception {
        var saved = userRepository.save(user());
        var token = jwt().jwt(b -> b.subject(saved.getId().toString()));

        mockMvc.perform(get("/api/v1/users/{id}", saved.getId()).with(token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user"));
    }

    @Test
    void getById_whenNotOwned_shouldReturn404() throws Exception {
        var saved = userRepository.save(user());

        mockMvc.perform(get("/api/v1/users/{id}", saved.getId()).with(jwt().jwt(b -> b.subject("999"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAll_shouldReturnPage() throws Exception {
        userRepository.save(user("alpha", "alpha@test.com"));
        userRepository.save(user("beta", "beta@test.com"));

        mockMvc.perform(get("/api/v1/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void create_shouldReturn201() throws Exception {
        mockMvc.perform(post("/api/v1/users").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UserRequestCreate("newuser", "new@test.com", "pass"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    void create_withInvalidBody_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/users").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserRequestCreate(null, null, null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.errors.length()").value(3));
    }

    @Test
    void update_shouldReturn200() throws Exception {
        var saved = userRepository.save(user());
        var token = jwt().jwt(b -> b.subject(saved.getId().toString()));

        mockMvc.perform(put("/api/v1/users").with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UserUpdateRequest("updated", "updated@test.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updated"));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        var saved = userRepository.save(user());
        var token = jwt().jwt(b -> b.subject(saved.getId().toString()));

        mockMvc.perform(delete("/api/v1/users").with(token))
                .andExpect(status().isNoContent());
    }
}
