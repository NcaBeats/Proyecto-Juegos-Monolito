package com.app.proyectojuegosmonolito.user.controller;

import com.app.proyectojuegosmonolito.TestcontainersConfiguration;
import com.app.proyectojuegosmonolito.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static com.app.proyectojuegosmonolito.user.UserFixtures.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@Transactional
class ProfileControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Test
    void getByUserId_shouldReturn200() throws Exception {
        var user = userService.create(user());

        mockMvc.perform(get("/api/v1/users/{userId}/profile", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("user"))
                .andExpect(jsonPath("$.visibility").value("PUBLIC"));
    }

    @Test
    void getByUserId_whenNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/users/999/profile"))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn200() throws Exception {
        var user = userService.create(user());

        mockMvc.perform(put("/api/v1/users/{userId}/profile", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "nickname": "new_nick",
                                    "avatarImage": "avatar.png",
                                    "bio": "My bio",
                                    "visibility": "PRIVATE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("new_nick"))
                .andExpect(jsonPath("$.visibility").value("PRIVATE"));
    }

    @Test
    void update_withInvalidBody_shouldReturn400() throws Exception {
        var user = userService.create(user());

        mockMvc.perform(put("/api/v1/users/{userId}/profile", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
