package com.app.proyectojuegosmonolito.game.service;

import com.app.proyectojuegosmonolito.TestcontainersConfiguration;
import com.app.proyectojuegosmonolito.game.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Transactional
class CategoryServiceIntegrationTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void create_shouldPersistCategory() {
        var category = com.app.proyectojuegosmonolito.game.model.Category.builder()
                .name("Action").build();

        var result = categoryService.create(category);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getCreatedAt()).isNotNull();

        var found = categoryRepository.findById(result.getId());
        assertThat(found).isPresent();
    }

    @Test
    void findById_shouldReturnCategory() {
        var saved = categoryRepository.save(
                com.app.proyectojuegosmonolito.game.model.Category.builder().name("RPG").build());

        var result = categoryService.findById(saved.getId());

        assertThat(result.getName()).isEqualTo("RPG");
    }

    @Test
    void findById_whenNotFound_shouldThrow() {
        assertThatThrownBy(() -> categoryService.findById(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void findAll_shouldReturnPage() {
        categoryRepository.save(com.app.proyectojuegosmonolito.game.model.Category.builder().name("Action").build());
        categoryRepository.save(com.app.proyectojuegosmonolito.game.model.Category.builder().name("RPG").build());

        var page = categoryService.findAll(PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(2);
    }

    @Test
    void update_shouldModifyAndSave() {
        var saved = categoryRepository.save(
                com.app.proyectojuegosmonolito.game.model.Category.builder().name("Old").build());

        var result = categoryService.update(saved.getId(), "New");

        assertThat(result.getName()).isEqualTo("New");
    }

    @Test
    void delete_shouldRemoveCategory() {
        var saved = categoryRepository.save(
                com.app.proyectojuegosmonolito.game.model.Category.builder().name("ToDelete").build());

        categoryService.delete(saved.getId());

        assertThat(categoryRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void delete_whenNotFound_shouldThrow() {
        assertThatThrownBy(() -> categoryService.delete(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
