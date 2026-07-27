package com.app.proyectojuegosmonolito.game.repository;

import com.app.proyectojuegosmonolito.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

import static com.app.proyectojuegosmonolito.game.GameFixtures.*;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GameRepositoryIT {

    @Autowired
    private GameRepository gameRepository;

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
}
