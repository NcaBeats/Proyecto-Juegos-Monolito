package com.app.proyectojuegosmonolito.game.service;

import com.app.proyectojuegosmonolito.TestcontainersConfiguration;
import com.app.proyectojuegosmonolito.game.model.GameState;
import com.app.proyectojuegosmonolito.game.repository.GameRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;

import static com.app.proyectojuegosmonolito.game.GameFixtures.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Transactional
class GameServiceIntegrationTest {

    @Autowired
    private GameService gameService;

    @Autowired
    private GameRepository gameRepository;

    @Test
    void create_shouldPersistGame() {
        var game = game();

        var result = gameService.create(game);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getCreatedAt()).isNotNull();

        var found = gameRepository.findById(result.getId());
        assertThat(found).isPresent();
    }

    @Test
    void findById_shouldReturnGame() {
        var saved = gameRepository.save(game());

        var result = gameService.findById(saved.getId());

        assertThat(result.getName()).isEqualTo("game");
    }

    @Test
    void findById_whenNotFound_shouldThrow() {
        assertThatThrownBy(() -> gameService.findById(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void findAll_shouldReturnPage() {
        gameRepository.saveAll(List.of(game("Alpha", BigDecimal.TEN),
                                       game("Beta", BigDecimal.TEN),
                                       game("Gamma", BigDecimal.TEN)));

        var page = gameService.findAll(PageRequest.of(0, 2));

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
    }

    @Test
    void findDiscounted_shouldReturnDiscountedGames() {
        gameRepository.save(game("Full Price", BigDecimal.TEN));
        gameRepository.save(gameWithDiscount("Discounted", BigDecimal.TEN, 50));

        var page = gameService.findDiscounted(PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getName()).isEqualTo("Discounted");
    }

    @Test
    void update_shouldModifyAndSave() {
        var saved = gameRepository.save(game());

        var result = gameService.update(saved.getId(), "Nuevo nombre",
                BigDecimal.TEN, 0, "desc", GameState.COMING_SOON, LocalDate.now(), new ArrayList<>());

        assertThat(result.getName()).isEqualTo("Nuevo nombre");
    }

    @Test
    void delete_shouldRemoveGame() {
        var saved = gameRepository.save(game());

        gameService.delete(saved.getId());

        assertThat(gameRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void delete_whenNotFound_shouldThrow() {
        assertThatThrownBy(() -> gameService.delete(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
