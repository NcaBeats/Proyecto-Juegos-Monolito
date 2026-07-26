package com.app.proyectojuegosmonolito.game.dto;

import com.app.proyectojuegosmonolito.game.model.GameState;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record GameRequest(
    @NotBlank @Size(max = 50) String name,
    @NotNull @DecimalMin("0") BigDecimal price,
    @NotBlank String description,
    @NotNull GameState state,
    @NotNull LocalDate launchDate
) {}
