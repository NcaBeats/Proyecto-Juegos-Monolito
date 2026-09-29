package com.app.proyectojuegosmonolito.account.user.service;

import com.app.proyectojuegosmonolito.TestcontainersConfiguration;
import com.app.proyectojuegosmonolito.account.profile.model.Visibility;
import com.app.proyectojuegosmonolito.account.user.repository.UserRepository;
import com.app.proyectojuegosmonolito.account.user.model.User;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.app.proyectojuegosmonolito.account.user.UserFixtures.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void create_shouldPersistUserWithProfileAndWallet() {
        var result = userService.create(user(), profile(user()));

        assertThat(result.getId()).isNotNull();
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getProfile()).isNotNull();
        assertThat(result.getProfile().getNickname()).isEqualTo("user@test.com");
        assertThat(result.getProfile().getVisibility()).isEqualTo(Visibility.PUBLIC);
        assertThat(result.getWallet()).isNotNull();
        assertThat(result.getWallet().getBalance()).isEqualByComparingTo("0");
    }

    @Test
    void findById_shouldReturnUser() {
        var saved = userService.create(user(), profile(user()));

        var result = userService.findById(saved.getId());

        assertThat(result.getEmail()).isEqualTo("user@test.com");
    }

    @Test
    void findById_whenNotFound_shouldThrow() {
        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void findAll_shouldReturnPage() {
        userRepository.saveAll(List.of(
                user("alpha@test.com"),
                user("beta@test.com"),
                user("gamma@test.com")
        ));

        var page = userService.findAll(PageRequest.of(0, 2));

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
    }

    @Test
    void update_shouldModifyAndSave() {
        var saved = userService.create(user(), profile(user()));
        var savedPassword = saved.getPassword();

        var result = userService.update(saved.getId(), "new@test.com");

        assertThat(result.getEmail()).isEqualTo("new@test.com");
        assertThat(result.getPassword()).isEqualTo(savedPassword);
    }

    @Test
    void delete_shouldSoftDelete() {
        var saved = userService.create(user(), profile(user()));

        userService.delete(saved.getId());

        assertThat(userRepository.findById(saved.getId())).isPresent();
        assertThat(userService.findAll(PageRequest.of(0, 10)).getContent())
                .extracting(User::getId)
                .doesNotContain(saved.getId());
        assertThatThrownBy(() -> userService.findByEmail(saved.getEmail()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void delete_whenNotFound_shouldThrow() {
        assertThatThrownBy(() -> userService.delete(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
