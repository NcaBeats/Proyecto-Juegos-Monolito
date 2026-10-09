package com.app.proyectojuegosmonolito.game.service;

import com.app.proyectojuegosmonolito.game.dto.GameRequest;
import com.app.proyectojuegosmonolito.game.model.Game;
import com.app.proyectojuegosmonolito.game.model.GameImage;
import com.app.proyectojuegosmonolito.game.model.GameState;
import com.app.proyectojuegosmonolito.game.repository.GameRepository;
import com.app.proyectojuegosmonolito.common.storage.R2StorageService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.app.proyectojuegosmonolito.game.GameFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private R2StorageService r2StorageService;

    @InjectMocks
    private GameService gameService;

    @Test
    void create_shouldSetCreatedAtAndSave() {
        var game = game();
        when(gameRepository.save(any())).thenAnswer(i -> {
            var g = i.<Game>getArgument(0);
            g.setId(1L);
            return g;
        });

        var result = gameService.create(game);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCreatedAt()).isNotNull();
        verify(gameRepository).save(game);
    }

    @Test
    void findById_whenFound_shouldReturnGame() {
        var game = game(1L);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        var result = gameService.findById(1L);

        assertThat(result).isEqualTo(game);
    }

    @Test
    void findById_whenNotFound_shouldThrow() {
        when(gameRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.findById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void findAll_shouldReturnPage() {
        var pageable = PageRequest.of(0, 10);
        var games = List.of(game(1L), game(2L));
        var page = new PageImpl<>(games, pageable, 2);
        when(gameRepository.findAll(pageable)).thenReturn(page);

        var result = gameService.findAll(pageable);

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void findDiscounted_shouldReturnPage() {
        var pageable = PageRequest.of(0, 10);
        var games = List.of(gameWithDiscount("Discounted", BigDecimal.TEN, 50));
        var page = new PageImpl<>(games, pageable, 1);
        when(gameRepository.findByDiscountPercentGreaterThan(0, pageable)).thenReturn(page);

        var result = gameService.findDiscounted(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getDiscountPercent()).isEqualTo(50);
        verify(gameRepository).findByDiscountPercentGreaterThan(0, pageable);
    }

    @Test
    void update_shouldModifyAndSave() {
        var game = game(1L, "Old", BigDecimal.ONE);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        var request = new GameRequest("New Name", new BigDecimal("49.99"), 10,
                "New desc", GameState.COMING_SOON, LocalDate.of(2027, 1, 1), List.of(),
                "min specs", "rec specs", null, 1L);

        var result = gameService.update(1L, request, new ArrayList<>());

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getOriginalPrice()).isEqualByComparingTo("49.99");
        assertThat(result.getDiscountPercent()).isEqualTo(10);
        assertThat(result.getPrice()).isEqualByComparingTo("44.99");
        assertThat(result.getState()).isEqualTo(GameState.COMING_SOON);
        assertThat(result.getMinimumSpecs()).isEqualTo("min specs");
        assertThat(result.getRecommendedSpecs()).isEqualTo("rec specs");
        verify(gameRepository, never()).save(any());
    }

    /**
     * Una edicion que omite videoUrl no debe vaciar el trailer almacenado: el
     * valor ausente se completa con el que ya tenia el juego.
     */
    @Test
    void update_whenVideoUrlOmitted_shouldPreserveExistingVideo() {
        var game = game(1L, "Old", BigDecimal.ONE);
        game.setVideoUrl("https://pub-test.r2.dev/old/trailer.mp4");
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        var request = new GameRequest("New Name", new BigDecimal("49.99"), 10,
                "New desc", GameState.COMING_SOON, LocalDate.of(2027, 1, 1), List.of(),
                "min specs", "rec specs", null, null);

        var result = gameService.update(1L, request, new ArrayList<>());

        assertThat(result.getVideoUrl()).isEqualTo("https://pub-test.r2.dev/old/trailer.mp4");
    }

    @Test
    void update_whenVideoUrlProvided_shouldReplaceExistingVideo() {
        var game = game(1L, "Old", BigDecimal.ONE);
        game.setVideoUrl("https://pub-test.r2.dev/old/trailer.mp4");
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        var request = new GameRequest("New Name", new BigDecimal("49.99"), 10,
                "New desc", GameState.COMING_SOON, LocalDate.of(2027, 1, 1), List.of(),
                "min specs", "rec specs", "https://pub-test.r2.dev/new/trailer.mp4", null);

        var result = gameService.update(1L, request, new ArrayList<>());

        assertThat(result.getVideoUrl()).isEqualTo("https://pub-test.r2.dev/new/trailer.mp4");
    }

    @Test
    void delete_whenExists_shouldDelete() {
        when(gameRepository.existsById(1L)).thenReturn(true);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game(1L, "Old", BigDecimal.ONE)));

        gameService.delete(1L);

        verify(gameRepository).deleteById(1L);
    }

    @Test
    void delete_shouldCleanUpImagesAndTheR2Trailer() {
        var game = game(1L, "Shadow Blade", BigDecimal.ONE);
        game.setImageUrl("https://pub-test.r2.dev/shadow-blade/card.jpg");
        game.setBannerUrl("https://pub-test.r2.dev/shadow-blade/banner.jpg");
        game.setVideoUrl("https://pub-test.r2.dev/shadow-blade/trailer.mp4");
        game.getGallery().add(GameImage.builder().game(game)
                .url("https://pub-test.r2.dev/shadow-blade/gallery/1.jpg")
                .position(0).build());
        when(gameRepository.existsById(1L)).thenReturn(true);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        gameService.delete(1L);

        verify(gameRepository).deleteById(1L);
        verify(r2StorageService).deleteImage(game.getImageUrl());
        verify(r2StorageService).deleteImage(game.getBannerUrl());
        verify(r2StorageService).deleteImage(game.getGallery().getFirst().getUrl());
        verify(r2StorageService).deleteVideo("shadow-blade");
    }

    @Test
    void delete_shouldSucceedEvenIfMediaCleanupFails() {
        var game = game(1L, "Shadow Blade", BigDecimal.ONE);
        game.setImageUrl("https://pub-test.r2.dev/shadow-blade/card.jpg");
        when(gameRepository.existsById(1L)).thenReturn(true);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        doThrow(new RuntimeException("r2 caido")).when(r2StorageService).deleteImage(anyString());

        // El juego ya esta borrado en base de datos: un almacen caido no debe
        // dejar el borrado a medias ni propagar el error.
        assertThatNoException().isThrownBy(() -> gameService.delete(1L));

        verify(gameRepository).deleteById(1L);
    }

    @Test
    void delete_whenNotFound_shouldThrow() {
        when(gameRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> gameService.delete(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(gameRepository, never()).deleteById(any());
    }

    // --- Invariante: toda URL de media que entra al servicio debe ser absoluta ---

    @Test
    void updateVideoUrl_whenRelativePath_shouldReject() {
        assertThatThrownBy(() -> gameService.updateVideoUrl(1L, "/uploads/games/old/trailer.mp4"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("videoUrl")
                .hasMessageContaining("URL absoluta");
    }

    @Test
    void updateImage_whenRelativePath_shouldReject() {
        assertThatThrownBy(() -> gameService.updateImage(1L, "/uploads/games/old/card.jpg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("imageUrl");
    }

    @Test
    void assignBanner_whenRelativePath_shouldReject() {
        assertThatThrownBy(() -> gameService.assignBanner(1L, "/uploads/games/old/banner.jpg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bannerUrl");
    }

    @Test
    void replaceGallery_whenOneUrlIsRelative_shouldReject() {
        assertThatThrownBy(() -> gameService.replaceGallery(1L,
                List.of("https://pub-test.r2.dev/a.jpg", "/uploads/games/old/b.jpg")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("galleryUrls");
    }

    @Test
    void applyMediaUrls_whenRelativeGallery_shouldReject() {
        assertThatThrownBy(() -> gameService.applyMediaUrls(1L, null, null,
                List.of("/uploads/games/old/b.jpg")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("galleryUrls");
    }

    /** Blank significa "no cambiar" en los endpoints de edicion parcial: debe seguir pasando. */
    @Test
    void applyMediaUrls_whenBlankMedia_shouldLeaveItIntact() {
        var game = game(1L, "Old", BigDecimal.ONE);
        game.setImageUrl("https://pub-test.r2.dev/previo.jpg");
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        var result = gameService.applyMediaUrls(1L, "  ", "", List.of());

        assertThat(result.getImageUrl()).isEqualTo("https://pub-test.r2.dev/previo.jpg");
    }

    @Test
    void create_whenVideoUrlIsRelative_shouldReject() {
        var game = game();
        game.setVideoUrl("/uploads/games/nuevo/trailer.mp4");

        assertThatThrownBy(() -> gameService.create(game))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("videoUrl");

        verify(gameRepository, never()).save(any());
    }
}
