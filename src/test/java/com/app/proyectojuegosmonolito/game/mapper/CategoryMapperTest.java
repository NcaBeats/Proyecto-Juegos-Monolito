package com.app.proyectojuegosmonolito.game.mapper;

import com.app.proyectojuegosmonolito.game.dto.CategoryRequest;
import com.app.proyectojuegosmonolito.game.dto.CategoryResponse;
import com.app.proyectojuegosmonolito.game.model.Category;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class CategoryMapperTest {

    private final CategoryMapper mapper = new CategoryMapper();

    @Test
    void toEntity_shouldMapAllFields() {
        var request = new CategoryRequest("Action");

        var result = mapper.toEntity(request);

        assertThat(result.getName()).isEqualTo("Action");
        assertThat(result.getId()).isNull();
        assertThat(result.getCreatedAt()).isNull();
    }

    @Test
    void toResponse_shouldMapAllFields() {
        var createdAt = Instant.parse("2026-01-01T00:00:00Z");
        var category = Category.builder()
                .id(1L)
                .name("Action")
                .createdAt(createdAt)
                .build();

        var result = mapper.toResponse(category);

        assertThat(result).isEqualTo(new CategoryResponse(1L, "Action", createdAt));
    }
}
