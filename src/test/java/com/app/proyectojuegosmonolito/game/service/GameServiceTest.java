package com.app.proyectojuegosmonolito.game.service;

import com.app.proyectojuegosmonolito.game.model.Game;
import com.app.proyectojuegosmonolito.game.model.GameState;
import com.app.proyectojuegosmonolito.game.repository.GameRepository;
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

        var result = gameService.update(1L, "New Name", new BigDecimal("49.99"), 10,
                "New desc", GameState.COMING_SOON, LocalDate.of(2027, 1, 1), new ArrayList<>());

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getOriginalPrice()).isEqualByComparingTo("49.99");
        assertThat(result.getDiscountPercent()).isEqualTo(10);
        assertThat(result.getPrice()).isEqualByComparingTo("44.99");
        assertThat(result.getState()).isEqualTo(GameState.COMING_SOON);
        verify(gameRepository, never()).save(any());
    }

    @Test
    void delete_whenExists_shouldDelete() {
        when(gameRepository.existsById(1L)).thenReturn(true);

        gameService.delete(1L);

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
}
