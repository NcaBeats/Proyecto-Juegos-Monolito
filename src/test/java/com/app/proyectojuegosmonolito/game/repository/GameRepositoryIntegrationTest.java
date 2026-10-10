package com.app.proyectojuegosmonolito.game.repository;

import com.app.proyectojuegosmonolito.TestcontainersConfiguration;
import com.app.proyectojuegosmonolito.game.model.Game;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

import static com.app.proyectojuegosmonolito.game.GameFixtures.*;
import static com.app.proyectojuegosmonolito.game.CategoryFixtures.*;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GameRepositoryIntegrationTest {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void saveAndFindById() {
        var saved = gameRepository.save(game());

        var found = gameRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("game");
        assertThat(found.get().getCreatedAt()).isNotNull();
    }

    @Test
    void findAllWithPagination() {
        gameRepository.saveAll(List.of(game("Alpha", BigDecimal.TEN), game("Beta", BigDecimal.TEN), game("Gamma", BigDecimal.TEN)));

        var page = gameRepository.findAll(PageRequest.of(0, 2));

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
    }

    @Test
    void saveWithGeneratedId() {
        var saved = gameRepository.save(game());

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void deleteById_shouldRemoveGame() {
        var saved = gameRepository.save(game());

        gameRepository.deleteById(saved.getId());

        assertThat(gameRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void search_byMaxPrice_shouldFilterOnEffectivePriceNotOriginal() {
        gameRepository.saveAll(List.of(
                game("Cheap", BigDecimal.TEN),
                gameWithDiscount("Discounted", new BigDecimal("50"), 90),
                game("Expensive", new BigDecimal("100"))));

        var page = gameRepository.search("%%", null, null, BigDecimal.TEN, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Game::getName)
                .containsExactlyInAnyOrder("Cheap", "Discounted");
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void search_byMinPrice_shouldExcludeCheaperGames() {
        gameRepository.saveAll(List.of(
                game("Cheap", BigDecimal.TEN),
                game("Expensive", new BigDecimal("100"))));

        var page = gameRepository.search("%%", null, new BigDecimal("50"), null, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Game::getName).containsExactly("Expensive");
        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    void search_combinedWithName_shouldApplyBothFilters() {
        gameRepository.saveAll(List.of(
                game("Alpha", BigDecimal.TEN),
                game("Alphabet", new BigDecimal("100"))));

        var page = gameRepository.search("%alpha%", null, null, BigDecimal.TEN, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Game::getName).containsExactly("Alpha");
        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    void search_withCategory_shouldReturnDistinctRows() {
        var multi = game("Multi", BigDecimal.TEN);
        multi.setCategories(List.of(
                categoryRepository.save(category("Action")),
                categoryRepository.save(category("Rpg"))));
        gameRepository.save(multi);

        var page = gameRepository.search("%%", null, null, null, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(1);
    }
}
