package com.app.proyectojuegosmonolito.account.user.mapper;

import com.app.proyectojuegosmonolito.account.profile.model.Comuna;
import com.app.proyectojuegosmonolito.account.profile.model.Region;
import com.app.proyectojuegosmonolito.account.profile.model.Visibility;
import com.app.proyectojuegosmonolito.account.user.dto.UserRequestCreate;
import com.app.proyectojuegosmonolito.account.user.dto.UserResponse;
import com.app.proyectojuegosmonolito.account.user.model.Role;
import com.app.proyectojuegosmonolito.account.user.model.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class UserMapperTest {

    private final UserMapper mapper = new UserMapper();

    @Test
    void toEntityCreate_shouldMapAllFields() {
        var request = new UserRequestCreate("test@test.com", "password123", "Test", "User",
                "190110222", LocalDate.of(2000, 1, 1),
                Region.METROPOLITANA_DE_SANTIAGO, Comuna.SANTIAGO, "Calle Test 123");

        var result = mapper.toEntityCreate(request);

        assertThat(result.getEmail()).isEqualTo("test@test.com");
        assertThat(result.getPassword()).isEqualTo("password123");
        assertThat(result.getId()).isNull();
        assertThat(result.getCreatedAt()).isNull();
    }

    @Test
    void toProfileCreate_shouldMapAllFields() {
        var request = new UserRequestCreate("test@test.com", "password123", "Test", "User",
                "190110222", LocalDate.of(2000, 1, 1),
                Region.METROPOLITANA_DE_SANTIAGO, Comuna.SANTIAGO, "Calle Test 123");
        var user = mapper.toEntityCreate(request);

        var result = mapper.toProfileCreate(request, user);

        assertThat(result.getNickname()).isEqualTo("test@test.com");
        assertThat(result.getRun()).isEqualTo("190110222");
        assertThat(result.getFirstName()).isEqualTo("Test");
        assertThat(result.getLastName()).isEqualTo("User");
        assertThat(result.getBirthDate()).isEqualTo(LocalDate.of(2000, 1, 1));
        assertThat(result.getRegion()).isEqualTo(Region.METROPOLITANA_DE_SANTIAGO);
        assertThat(result.getComuna()).isEqualTo(Comuna.SANTIAGO);
        assertThat(result.getAddress()).isEqualTo("Calle Test 123");
        assertThat(result.getVisibility()).isEqualTo(Visibility.PUBLIC);
    }

    @Test
    void toResponse_shouldMapAllFields() {
        var createdAt = Instant.parse("2026-01-01T00:00:00Z");
        var user = User.builder()
                .id(1L).email("test@test.com").role(Role.CLIENTE)
                .createdAt(createdAt)
                .build();

        var result = mapper.toResponse(user);

        assertThat(result).isEqualTo(new UserResponse(1L, "test@test.com", Role.CLIENTE, createdAt));
    }
}
