package com.app.proyectojuegosmonolito.account.user.dto;

import java.time.Instant;

public record UserResponse(
    Long id,
    String username,
    String email,
    Instant createdAt
) {}
