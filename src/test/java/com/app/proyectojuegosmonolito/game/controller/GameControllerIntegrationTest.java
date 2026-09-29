package com.app.proyectojuegosmonolito.game.controller;

import com.app.proyectojuegosmonolito.TestcontainersConfiguration;
import com.app.proyectojuegosmonolito.account.user.model.Role;
import com.app.proyectojuegosmonolito.account.user.model.User;
import com.app.proyectojuegosmonolito.account.user.service.UserService;
import com.app.proyectojuegosmonolito.game.dto.GameRequest;
import com.app.proyectojuegosmonolito.game.dto.GameWithMediaRequest;
import com.app.proyectojuegosmonolito.game.dto.ImagePresignRequest;
import com.app.proyectojuegosmonolito.game.dto.SignedImageUploadResponse;
import com.app.proyectojuegosmonolito.game.dto.VideoPresignRequest;
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

import org.mockito.ArgumentCaptor;
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

    private User createCliente() {
        var cliente = User.builder()
                .email("gamecliente@test.com")
                .password("pass123")
                .role(Role.CLIENTE)
                .createdAt(java.time.Instant.now())
                .build();
        return userService.create(cliente, profile(cliente));
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

    // --- Presign de trailer contra R2 ---

    @Test
    void presignVideo_shouldReturn200WithSignedUrl() throws Exception {
        var body = new VideoPresignRequest("Half-Life 3", "video/mp4");

        mockMvc.perform(post("/api/v1/games/media/video/presign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("half-life-3/trailer.mp4"))
                .andExpect(jsonPath("$.publicPath").value("/uploads/games/half-life-3/trailer.mp4"))
                .andExpect(jsonPath("$.contentType").value("video/mp4"))
                .andExpect(jsonPath("$.expiresInSeconds").value(900))
                .andExpect(jsonPath("$.uploadUrl").value(org.hamcrest.Matchers.containsString("X-Amz-Signature")))
                .andExpect(jsonPath("$.uploadUrl").value(org.hamcrest.Matchers.containsString("X-Amz-Expires=900")));
    }

    @Test
    void presignVideo_shouldSignTheContentTypeHeader() throws Exception {
        var body = new VideoPresignRequest("Cyberpunk", "video/webm");

        mockMvc.perform(post("/api/v1/games/media/video/presign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                // Si el content-type no fuera signed, R2 aceptaria cualquier header
                // en el PUT y la allowlist del backend seria evadible desde el cliente.
                .andExpect(jsonPath("$.uploadUrl").value(org.hamcrest.Matchers.containsString("SignedHeaders=content-type")))
                .andExpect(jsonPath("$.contentType").value("video/webm"));
    }

    @Test
    void presignVideo_withDisallowedContentType_shouldReturn400() throws Exception {
        var body = new VideoPresignRequest("Hack", "application/x-msdownload");

        mockMvc.perform(post("/api/v1/games/media/video/presign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void presignVideo_withBlankName_shouldReturn400() throws Exception {
        var body = new VideoPresignRequest("  ", "video/mp4");

        mockMvc.perform(post("/api/v1/games/media/video/presign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void presignVideo_whenAuthenticatedAsClient_shouldReturn403() throws Exception {
        var cliente = createCliente();
        var body = new VideoPresignRequest("Hack", "video/mp4");

        mockMvc.perform(post("/api/v1/games/media/video/presign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body))
                        .with(jwt().jwt(b -> b.subject(cliente.getId().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void presignVideo_withoutToken_shouldReturn401() throws Exception {
        var body = new VideoPresignRequest("Hack", "video/mp4");

        mockMvc.perform(post("/api/v1/games/media/video/presign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body)))
                .andExpect(status().isUnauthorized());
    }

    // --- Presign de imagen contra Cloudinary ---

    @Test
    void presignImage_kindImage_shouldUseCardFolder() throws Exception {
        assertPresignImageFolder("image", "Half-Life 3", "games/half-life-3/card");
    }

    @Test
    void presignImage_kindBanner_shouldUseBannerFolder() throws Exception {
        assertPresignImageFolder("banner", "Half-Life 3", "games/half-life-3/banner");
    }

    @Test
    void presignImage_kindGallery_shouldUseGalleryFolder() throws Exception {
        assertPresignImageFolder("gallery", "Half-Life 3", "games/half-life-3/gallery");
    }

    private void assertPresignImageFolder(String kind, String name, String expectedFolder) throws Exception {
        when(imageService.signUpload(anyString())).thenReturn(new SignedImageUploadResponse(
                "https://api.cloudinary.com/v1_1/test-cloud/image/upload",
                "test-cloud", "000000000000000", 1_700_000_000L, "deadbeef", expectedFolder));

        var body = new ImagePresignRequest(name, kind);

        mockMvc.perform(post("/api/v1/games/media/image/presign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cloudName").value("test-cloud"))
                .andExpect(jsonPath("$.signature").value("deadbeef"));

        var folderCaptor = ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(imageService).signUpload(folderCaptor.capture());
        org.assertj.core.api.Assertions.assertThat(folderCaptor.getValue()).isEqualTo(expectedFolder);
    }

    @Test
    void presignImage_withInvalidKind_shouldReturn400() throws Exception {
        var body = new ImagePresignRequest("Half-Life 3", "trailer");

        mockMvc.perform(post("/api/v1/games/media/image/presign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void presignImage_whenAuthenticatedAsClient_shouldReturn403() throws Exception {
        var cliente = createCliente();
        var body = new ImagePresignRequest("Hack", "image");

        mockMvc.perform(post("/api/v1/games/media/image/presign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body))
                        .with(jwt().jwt(b -> b.subject(cliente.getId().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
                .andExpect(status().isForbidden());
    }

    // --- Alta y edicion por JSON con media ya subida ---

    @Test
    void createWithMediaUrls_shouldReturn201AndPersistMedia() throws Exception {
        var admin = createAdmin();
        var body = new GameWithMediaRequest(
                "Juego Con Media", new BigDecimal("29.99"), 10, "Descripción",
                GameState.AVAILABLE, LocalDate.of(2026, 12, 1), List.of("Action"),
                null, null,
                "/uploads/games/juego-con-media/trailer.mp4", null,
                "https://res.cloudinary.com/test/image/upload/card.jpg",
                "https://res.cloudinary.com/test/image/upload/banner.jpg",
                List.of("https://res.cloudinary.com/test/image/upload/gal1.jpg",
                        "https://res.cloudinary.com/test/image/upload/gal2.jpg"));

        mockMvc.perform(post("/api/v1/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body))
                        .with(jwt().jwt(b -> b.subject(admin.getId().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Juego Con Media"))
                .andExpect(jsonPath("$.videoUrl").value("/uploads/games/juego-con-media/trailer.mp4"))
                .andExpect(jsonPath("$.imageUrl").value("https://res.cloudinary.com/test/image/upload/card.jpg"))
                .andExpect(jsonPath("$.bannerUrl").value("https://res.cloudinary.com/test/image/upload/banner.jpg"))
                .andExpect(jsonPath("$.galleryUrls.length()").value(2));
    }

    @Test
    void createWithMediaUrls_whenAuthenticatedAsClient_shouldReturn403() throws Exception {
        var cliente = createCliente();
        var body = new GameWithMediaRequest(
                "Juego Con Media", new BigDecimal("29.99"), 10, "Descripción",
                GameState.AVAILABLE, LocalDate.of(2026, 12, 1), List.of("Action"),
                null, null, null, null, null, null, null);

        mockMvc.perform(post("/api/v1/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body))
                        .with(jwt().jwt(b -> b.subject(cliente.getId().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateWithMediaUrls_shouldReturn200AndPreserveOmittedMedia() throws Exception {
        var saved = gameRepository.save(game());
        var admin = createAdmin();
        // Regresion: antes este endpoint borraba el videoUrl cuando el body lo omitia.
        saved.setVideoUrl("/uploads/games/previo/trailer.mp4");
        saved.setImageUrl("https://res.cloudinary.com/test/image/upload/previa.jpg");
        gameRepository.saveAndFlush(saved);

        var body = new GameWithMediaRequest(
                "Juego Con Media", new BigDecimal("49.99"), 20, "Nueva desc",
                GameState.AVAILABLE, LocalDate.of(2026, 12, 1), List.of("Action"),
                null, null, null, null, null, null, null);

        mockMvc.perform(put("/api/v1/games/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body))
                        .with(jwt().jwt(b -> b.subject(admin.getId().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Juego Con Media"))
                .andExpect(jsonPath("$.originalPrice").value(49.99))
                .andExpect(jsonPath("$.videoUrl").value("/uploads/games/previo/trailer.mp4"))
                .andExpect(jsonPath("$.imageUrl").value("https://res.cloudinary.com/test/image/upload/previa.jpg"));
    }

    @Test
    void updateWithMediaUrls_shouldReplaceMediaWhenProvided() throws Exception {
        var saved = gameRepository.save(game());
        var admin = createAdmin();

        var body = new GameWithMediaRequest(
                "Juego Con Media", new BigDecimal("49.99"), 20, "Nueva desc",
                GameState.AVAILABLE, LocalDate.of(2026, 12, 1), List.of("Action"),
                null, null,
                "/uploads/games/nuevo/trailer.mp4", null,
                "https://res.cloudinary.com/test/image/upload/nueva.jpg", null,
                List.of("https://res.cloudinary.com/test/image/upload/nueva-gal.jpg"));

        mockMvc.perform(put("/api/v1/games/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body))
                        .with(jwt().jwt(b -> b.subject(admin.getId().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.videoUrl").value("/uploads/games/nuevo/trailer.mp4"))
                .andExpect(jsonPath("$.imageUrl").value("https://res.cloudinary.com/test/image/upload/nueva.jpg"))
                .andExpect(jsonPath("$.galleryUrls.length()").value(1));
    }
}
