package com.app.proyectojuegosmonolito.game.controller;

import com.app.proyectojuegosmonolito.TestcontainersConfiguration;
import com.app.proyectojuegosmonolito.account.user.model.Role;
import com.app.proyectojuegosmonolito.account.user.model.User;
import com.app.proyectojuegosmonolito.account.user.service.UserService;
import com.app.proyectojuegosmonolito.game.dto.GameRequest;
import com.app.proyectojuegosmonolito.game.dto.GameWithMediaRequest;
import com.app.proyectojuegosmonolito.game.dto.ImagePresignRequest;
import com.app.proyectojuegosmonolito.game.dto.VideoPresignRequest;
import com.app.proyectojuegosmonolito.game.model.GameState;
import com.app.proyectojuegosmonolito.game.repository.GameRepository;
import com.app.proyectojuegosmonolito.game.storage.R2StorageService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
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
import static org.mockito.Mockito.doReturn;
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

    @MockitoSpyBean
    private R2StorageService r2StorageService;

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
        doReturn("https://example.com/img.png")
                .when(r2StorageService).storeImage(any(MultipartFile.class), anyString(), anyString());
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
        var body = new VideoPresignRequest("Half-Life 3", "video/mp4", null);

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
        var body = new VideoPresignRequest("Cyberpunk", "video/webm", null);

        mockMvc.perform(post("/api/v1/games/media/video/presign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                // Si el content-type no fuera signed, R2 aceptaria cualquier header
                // en el PUT y la allowlist del backend seria evadible desde el cliente.
                // Los signed headers van en orden alfabetico y URL-encoded, y a
                // content-type se le suma cache-control: ambos tienen que viajar
                // firmados o el header no se guarda.
                .andExpect(jsonPath("$.uploadUrl").value(org.hamcrest.Matchers.containsString(
                        "X-Amz-SignedHeaders=cache-control%3Bcontent-type")))
                .andExpect(jsonPath("$.contentType").value("video/webm"));
    }

    @Test
    void presignVideo_shouldSignTheCacheControlHeader() throws Exception {
        var body = new VideoPresignRequest("Cyberpunk", "video/mp4", null);

        mockMvc.perform(post("/api/v1/games/media/video/presign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                // R2 solo persiste un Cache-Control que el servidor haya firmado. Si
                // no estuviera entre los signed headers, el navegador podria omitirlo
                // y el trailer se guardaria sin cachear, sin error visible.
                .andExpect(jsonPath("$.uploadUrl").value(org.hamcrest.Matchers.containsString(
                        "X-Amz-SignedHeaders=cache-control%3Bcontent-type")))
                // El valor vuelve al cliente para que reenvie exactamente el mismo
                // string; duplicar la constante en el frontend es como aparecen los
                // 403 SignatureDoesNotMatch.
                .andExpect(jsonPath("$.cacheControl").value(
                        "public, max-age=31536000, immutable"));
    }

    @Test
    void presignVideo_withFingerprint_shouldUseVersionedKey() throws Exception {
        var body = new VideoPresignRequest("Grand Theft Auto V", "video/mp4", "a1b2c3d4");

        mockMvc.perform(post("/api/v1/games/media/video/presign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                // La version viaja en el nombre del archivo, no en un query param:
                // asi el navegador guarda la respuesta como inmutable sin pedirla de nuevo.
                .andExpect(jsonPath("$.key").value("grand-theft-auto-v/trailer-a1b2c3d4.mp4"))
                .andExpect(jsonPath("$.publicPath").value(
                        "/uploads/games/grand-theft-auto-v/trailer-a1b2c3d4.mp4"))
                .andExpect(jsonPath("$.cacheControl").value(
                        "public, max-age=31536000, immutable"));
    }

    @Test
    void presignVideo_withMalformedFingerprint_shouldReturn400() throws Exception {
        var body = new VideoPresignRequest("Grand Theft Auto V", "video/mp4", "../../evil");

        mockMvc.perform(post("/api/v1/games/media/video/presign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                // La huella se interpola en la clave, asi que un valor no validado
                // permitiria escribir fuera del directorio del juego.
                .andExpect(status().isBadRequest());
    }

    @Test
    void presignVideo_withDisallowedContentType_shouldReturn400() throws Exception {
        var body = new VideoPresignRequest("Hack", "application/x-msdownload", null);

        mockMvc.perform(post("/api/v1/games/media/video/presign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void presignVideo_withBlankName_shouldReturn400() throws Exception {
        var body = new VideoPresignRequest("  ", "video/mp4", null);

        mockMvc.perform(post("/api/v1/games/media/video/presign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void presignVideo_whenAuthenticatedAsClient_shouldReturn403() throws Exception {
        var cliente = createCliente();
        var body = new VideoPresignRequest("Hack", "video/mp4", null);

        mockMvc.perform(post("/api/v1/games/media/video/presign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body))
                        .with(jwt().jwt(b -> b.subject(cliente.getId().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void presignVideo_withoutToken_shouldReturn401() throws Exception {
        var body = new VideoPresignRequest("Hack", "video/mp4", null);

        mockMvc.perform(post("/api/v1/games/media/video/presign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body)))
                .andExpect(status().isUnauthorized());
    }

    // --- Presign de imagen contra R2 ---

    @Test
    void presignImage_kindImage_shouldUseCardKey() throws Exception {
        assertPresignImageKey("image", "Half-Life 3", "half-life-3/card/", "image/png");
    }

    @Test
    void presignImage_kindBanner_shouldUseBannerKey() throws Exception {
        assertPresignImageKey("banner", "Half-Life 3", "half-life-3/banner/", "image/webp");
    }

    @Test
    void presignImage_kindGallery_shouldUseGalleryKey() throws Exception {
        assertPresignImageKey("gallery", "Half-Life 3", "half-life-3/gallery/", "image/avif");
    }

    private void assertPresignImageKey(String kind, String name, String expectedKeyPrefix, String contentType) throws Exception {
        var body = new ImagePresignRequest(name, kind, contentType);

        mockMvc.perform(post("/api/v1/games/media/image/presign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value(org.hamcrest.Matchers.startsWith(expectedKeyPrefix)))
                .andExpect(jsonPath("$.key").value(org.hamcrest.Matchers.endsWith("." + contentType.substring(6))))
                .andExpect(jsonPath("$.publicPath").value(org.hamcrest.Matchers.containsString(expectedKeyPrefix)))
                .andExpect(jsonPath("$.contentType").value(contentType))
                .andExpect(jsonPath("$.cacheControl").value("public, max-age=31536000, immutable"))
                .andExpect(jsonPath("$.expiresInSeconds").value(900))
                .andExpect(jsonPath("$.uploadUrl").value(org.hamcrest.Matchers.containsString("X-Amz-Signature")));
    }

    @Test
    void presignImage_shouldSignTheCacheControlHeader() throws Exception {
        var body = new ImagePresignRequest("Half-Life 3", "image", "image/png");

        mockMvc.perform(post("/api/v1/games/media/image/presign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                // La clave de imagen ya lleva un UUID, asi que es unica por version y
                // admite immutable; lo que falta es que R2 lo firme para que se guarde.
                .andExpect(jsonPath("$.uploadUrl").value(org.hamcrest.Matchers.containsString(
                        "X-Amz-SignedHeaders=cache-control%3Bcontent-type")))
                .andExpect(jsonPath("$.cacheControl").value("public, max-age=31536000, immutable"));
    }

    @Test
    void presignImage_withInvalidKind_shouldReturn400() throws Exception {
        var body = new ImagePresignRequest("Half-Life 3", "trailer", "image/png");

        mockMvc.perform(post("/api/v1/games/media/image/presign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void presignImage_withDisallowedContentType_shouldReturn400() throws Exception {
        var body = new ImagePresignRequest("Half-Life 3", "image", "application/x-msdownload");

        mockMvc.perform(post("/api/v1/games/media/image/presign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void presignImage_whenAuthenticatedAsClient_shouldReturn403() throws Exception {
        var cliente = createCliente();
        var body = new ImagePresignRequest("Hack", "image", "image/png");

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
                "https://pub-test.r2.dev/card.jpg",
                "https://pub-test.r2.dev/banner.jpg",
                List.of("https://pub-test.r2.dev/gal1.jpg",
                        "https://pub-test.r2.dev/gal2.jpg"));

        mockMvc.perform(post("/api/v1/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body))
                        .with(jwt().jwt(b -> b.subject(admin.getId().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Juego Con Media"))
                .andExpect(jsonPath("$.videoUrl").value("/uploads/games/juego-con-media/trailer.mp4"))
                .andExpect(jsonPath("$.imageUrl").value("https://pub-test.r2.dev/card.jpg"))
                .andExpect(jsonPath("$.bannerUrl").value("https://pub-test.r2.dev/banner.jpg"))
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
        saved.setImageUrl("https://pub-test.r2.dev/previa.jpg");
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
                .andExpect(jsonPath("$.imageUrl").value("https://pub-test.r2.dev/previa.jpg"));
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
                "https://pub-test.r2.dev/nueva.jpg", null,
                List.of("https://pub-test.r2.dev/nueva-gal.jpg"));

        mockMvc.perform(put("/api/v1/games/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body))
                        .with(jwt().jwt(b -> b.subject(admin.getId().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.videoUrl").value("/uploads/games/nuevo/trailer.mp4"))
                .andExpect(jsonPath("$.imageUrl").value("https://pub-test.r2.dev/nueva.jpg"))
                .andExpect(jsonPath("$.galleryUrls.length()").value(1));
    }
}
