package com.app.proyectojuegosmonolito.account.user.repository;

import com.app.proyectojuegosmonolito.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static com.app.proyectojuegosmonolito.account.user.UserFixtures.*;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void save_shouldPersistWithGeneratedId() {
        var saved = userRepository.save(user());

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void findById_shouldReturnUser() {
        var saved = userRepository.save(user());

        var found = userRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("user@test.com");
    }

    @Test
    void findAllWithPagination() {
        userRepository.saveAll(List.of(
                user("alpha@test.com"),
                user("beta@test.com"),
                user("gamma@test.com")
        ));

        var page = userRepository.findAll(PageRequest.of(0, 2));

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
    }

    @Test
    void deleteById_shouldRemove() {
        var saved = userRepository.save(user());

        userRepository.deleteById(saved.getId());

        assertThat(userRepository.findById(saved.getId())).isEmpty();
    }
}
