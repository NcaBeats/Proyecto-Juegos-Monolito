package com.app.proyectojuegosmonolito.game.dto;

import java.math.BigDecimal;

public record GameStatsResponse(
    long total,
    long active,
    BigDecimal catalogValue
) {}