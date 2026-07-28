package com.app.proyectojuegosmonolito.library.repository;

import com.app.proyectojuegosmonolito.TestcontainersConfiguration;
import com.app.proyectojuegosmonolito.game.repository.GameRepository;
import com.app.proyectojuegosmonolito.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;

import static com.app.proyectojuegosmonolito.game.GameFixtures.*;
import static com.app.proyectojuegosmonolito.library.LibraryFixtures.*;
import static com.app.proyectojuegosmonolito.user.UserFixtures.*;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LibraryRepositoryIT {

    @Autowired
    private LibraryRepository libraryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Test
    void save_shouldPersistWithGeneratedId() {
        var user = userRepository.save(user());
        var game = gameRepository.save(game());
        var library = libraryRepository.save(library(user, game));

        assertThat(library.getId()).isNotNull();
    }

    @Test
    void findById_shouldReturnLibrary() {
        var user = userRepository.save(user());
        var game = gameRepository.save(game());
        var saved = libraryRepository.save(library(user, game));

        var found = libraryRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getUser().getId()).isEqualTo(user.getId());
        assertThat(found.get().getGame().getId()).isEqualTo(game.getId());
    }

    @Test
    void findByUserId_shouldReturnPage() {
        var user = userRepository.save(user());
        var game1 = gameRepository.save(game("Alpha", BigDecimal.TEN));
        var game2 = gameRepository.save(game("Beta", BigDecimal.TEN));
        libraryRepository.save(library(user, game1));
        libraryRepository.save(library(user, game2));

        var page = libraryRepository.findByUser_Id(user.getId(), PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(2);
    }

    @Test
    void existsByUserIdAndGameId_whenOwned_shouldReturnTrue() {
        var user = userRepository.save(user());
        var game = gameRepository.save(game());
        libraryRepository.save(library(user, game));

        assertThat(libraryRepository.existsByUser_IdAndGame_Id(user.getId(), game.getId())).isTrue();
    }

    @Test
    void existsByUserIdAndGameId_whenNotOwned_shouldReturnFalse() {
        assertThat(libraryRepository.existsByUser_IdAndGame_Id(999L, 999L)).isFalse();
    }

    @Test
    void deleteById_shouldRemove() {
        var user = userRepository.save(user());
        var game = gameRepository.save(game());
        var saved = libraryRepository.save(library(user, game));

        libraryRepository.deleteById(saved.getId());

        assertThat(libraryRepository.findById(saved.getId())).isEmpty();
    }
}
