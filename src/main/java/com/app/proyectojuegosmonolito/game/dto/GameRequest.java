package com.app.proyectojuegosmonolito.game.dto;

import com.app.proyectojuegosmonolito.game.model.GameState;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record GameRequest(
    @NotBlank @Size(max = 50) String name,
    @NotNull @DecimalMin("0") BigDecimal originalPrice,
    @NotNull @Min(0) @Max(100) Integer discountPercent,
    @NotBlank String description,
    @NotNull GameState state,
    @NotNull LocalDate launchDate,
    @NotNull @NotEmpty List<String> categoryNames
) {}
