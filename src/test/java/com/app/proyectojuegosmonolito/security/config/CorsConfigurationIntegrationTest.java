package com.app.proyectojuegosmonolito.security.config;

import com.app.proyectojuegosmonolito.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties =
        "app.cors.allowed-origins=http://localhost:3000,https://steam-clone-plum.vercel.app,https://steam-clone-*.vercel.app")
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@Transactional
class CorsConfigurationIntegrationTest {

    private static final String ALIAS_ORIGIN = "https://steam-clone-plum.vercel.app";
    private static final String PREVIEW_ORIGIN =
            "https://steam-clone-i9iv5qrpf-ncabeats-projects.vercel.app";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getGames_fromAliasOrigin_shouldAllowWithCredentials() throws Exception {
        mockMvc.perform(get("/api/v1/games").header("Origin", ALIAS_ORIGIN))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ALIAS_ORIGIN))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    void getGames_fromPreviewOrigin_shouldAllowWithCredentials() throws Exception {
        mockMvc.perform(get("/api/v1/games").header("Origin", PREVIEW_ORIGIN))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", PREVIEW_ORIGIN))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    void preflightFromPreviewOrigin_shouldAllowGet() throws Exception {
        mockMvc.perform(options("/api/v1/games")
                        .header("Origin", PREVIEW_ORIGIN)
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", PREVIEW_ORIGIN))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("GET")));
    }

    @Test
    void getGames_fromUnknownOrigin_shouldReject() throws Exception {
        mockMvc.perform(get("/api/v1/games").header("Origin", "https://sitio-ajeno.example"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
