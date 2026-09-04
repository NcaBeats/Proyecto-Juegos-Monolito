package com.app.proyectojuegosmonolito.game.dto;

import com.app.proyectojuegosmonolito.game.model.GameState;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record GameResponse(
    Long id,
    String name,
    BigDecimal originalPrice,
    BigDecimal price,
    Integer discountPercent,
    String description,
    GameState state,
    LocalDate launchDate,
    List<CategoryResponse> categories,
    String imageUrl,
    String bannerUrl,
    String minimumSpecs,
    String recommendedSpecs,
    Instant createdAt
) {}
