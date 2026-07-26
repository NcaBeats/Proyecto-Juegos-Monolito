package com.app.proyectojuegosmonolito.game.dto;

import com.app.proyectojuegosmonolito.game.model.GameState;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record GameResponse(
    Long id,
    String name,
    BigDecimal price,
    String description,
    GameState state,
    LocalDate launchDate,
    Instant createdAt
) {}
