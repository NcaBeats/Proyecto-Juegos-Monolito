package com.app.proyectojuegosmonolito.library.service;

import com.app.proyectojuegosmonolito.TestcontainersConfiguration;
import com.app.proyectojuegosmonolito.game.repository.GameRepository;
import com.app.proyectojuegosmonolito.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static com.app.proyectojuegosmonolito.game.GameFixtures.*;
import static com.app.proyectojuegosmonolito.user.UserFixtures.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class LibraryServiceIT {

    @Autowired
    private LibraryService libraryService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Test
    void add_shouldCreateEntry() {
        var user = userRepository.save(user());
        var game = gameRepository.save(game());

        var result = libraryService.add(user.getId(), game.getId());

        assertThat(result.getId()).isNotNull();
        assertThat(result.getAcquiredAt()).isNotNull();
    }

    @Test
    void add_whenAlreadyOwned_shouldThrow() {
        var user = userRepository.save(user());
        var game = gameRepository.save(game());
        libraryService.add(user.getId(), game.getId());

        assertThatThrownBy(() -> libraryService.add(user.getId(), game.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already in library");
    }

    @Test
    void add_withNonExistentUser_shouldThrow() {
        assertThatThrownBy(() -> libraryService.add(999L, 1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void add_withNonExistentGame_shouldThrow() {
        var user = userRepository.save(user());

        assertThatThrownBy(() -> libraryService.add(user.getId(), 999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void findByUserId_shouldReturnPage() {
        var user = userRepository.save(user());
        var game1 = gameRepository.save(game("Alpha", BigDecimal.TEN));
        var game2 = gameRepository.save(game("Beta", BigDecimal.TEN));
        libraryService.add(user.getId(), game1.getId());
        libraryService.add(user.getId(), game2.getId());

        var page = libraryService.findByUserId(user.getId(), PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(2);
    }

    @Test
    void isGameOwned_whenOwned_shouldReturnTrue() {
        var user = userRepository.save(user());
        var game = gameRepository.save(game());
        libraryService.add(user.getId(), game.getId());

        assertThat(libraryService.isGameOwned(user.getId(), game.getId())).isTrue();
    }

    @Test
    void isGameOwned_whenNotOwned_shouldReturnFalse() {
        assertThat(libraryService.isGameOwned(999L, 999L)).isFalse();
    }

    @Test
    void remove_shouldDelete() {
        var user = userRepository.save(user());
        var game = gameRepository.save(game());
        var lib = libraryService.add(user.getId(), game.getId());

        libraryService.remove(lib.getId(), user.getId());

        assertThat(libraryService.isGameOwned(user.getId(), game.getId())).isFalse();
    }

    @Test
    void remove_whenNotFound_shouldThrow() {
        assertThatThrownBy(() -> libraryService.remove(999L, 1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void remove_whenNotOwnedByUser_shouldThrow() {
        var user = userRepository.save(user("user1", "user1@test.com"));
        var otherUser = userRepository.save(user("user2", "user2@test.com"));
        var game = gameRepository.save(game());
        var lib = libraryService.add(user.getId(), game.getId());

        assertThatThrownBy(() -> libraryService.remove(lib.getId(), otherUser.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not belong to user");
    }
}
