package com.app.proyectojuegosmonolito.game.dto;

import java.time.Instant;

public record CategoryResponse(
    Long id,
    String name,
    Instant createdAt
) {}
