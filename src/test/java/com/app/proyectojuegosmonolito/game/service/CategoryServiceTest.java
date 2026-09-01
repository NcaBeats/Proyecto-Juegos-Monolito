package com.app.proyectojuegosmonolito.game.service;

import com.app.proyectojuegosmonolito.game.model.Category;
import com.app.proyectojuegosmonolito.game.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void create_shouldSetCreatedAtAndSave() {
        var category = Category.builder().name("Action").build();
        when(categoryRepository.save(any())).thenAnswer(i -> {
            var c = i.<Category>getArgument(0);
            c.setId(1L);
            return c;
        });

        var result = categoryService.create(category);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCreatedAt()).isNotNull();
        verify(categoryRepository).save(category);
    }

    @Test
    void findById_whenFound_shouldReturnCategory() {
        var category = Category.builder().id(1L).name("Action").build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        var result = categoryService.findById(1L);

        assertThat(result).isEqualTo(category);
    }

    @Test
    void findById_whenNotFound_shouldThrow() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void findAll_shouldReturnPage() {
        var pageable = PageRequest.of(0, 10);
        var categories = List.of(
                Category.builder().id(1L).name("Action").build(),
                Category.builder().id(2L).name("RPG").build()
        );
        var page = new PageImpl<>(categories, pageable, 2);
        when(categoryRepository.findAll(pageable)).thenReturn(page);

        var result = categoryService.findAll(pageable);

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void update_shouldModifyAndSave() {
        var category = Category.builder().id(1L).name("Old Name").build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        var result = categoryService.update(1L, "New Name");

        assertThat(result.getName()).isEqualTo("New Name");
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void delete_whenExists_shouldDelete() {
        when(categoryRepository.existsById(1L)).thenReturn(true);

        categoryService.delete(1L);

        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void delete_whenNotFound_shouldThrow() {
        when(categoryRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> categoryService.delete(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(categoryRepository, never()).deleteById(any());
    }
}
