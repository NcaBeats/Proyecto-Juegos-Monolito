package com.app.proyectojuegosmonolito.account.user.dto;

import com.app.proyectojuegosmonolito.account.profile.dto.ProfileResponse;
import com.app.proyectojuegosmonolito.account.user.model.Role;

import java.time.Instant;

public record AdminUserResponse(
        Long id,
        String email,
        Role role,
        Instant createdAt,
        ProfileResponse profile
) {}
