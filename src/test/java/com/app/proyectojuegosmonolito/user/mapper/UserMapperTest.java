package com.app.proyectojuegosmonolito.user.mapper;

import com.app.proyectojuegosmonolito.user.dto.UserRequest;
import com.app.proyectojuegosmonolito.user.dto.UserResponse;
import com.app.proyectojuegosmonolito.user.model.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class UserMapperTest {

    private final UserMapper mapper = new UserMapper();

    @Test
    void toEntity_shouldMapAllFields() {
        var request = new UserRequest("testuser", "test@test.com", "password123");

        var result = mapper.toEntity(request);

        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@test.com");
        assertThat(result.getPassword()).isEqualTo("password123");
        assertThat(result.getId()).isNull();
        assertThat(result.getCreatedAt()).isNull();
    }

    @Test
    void toResponse_shouldMapAllFields() {
        var createdAt = Instant.parse("2026-01-01T00:00:00Z");
        var user = User.builder()
                .id(1L).username("testuser").email("test@test.com")
                .createdAt(createdAt)
                .build();

        var result = mapper.toResponse(user);

        assertThat(result).isEqualTo(new UserResponse(1L, "testuser", "test@test.com", createdAt));
    }
}
