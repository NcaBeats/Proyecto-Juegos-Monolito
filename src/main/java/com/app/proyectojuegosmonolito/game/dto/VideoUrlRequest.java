package com.app.proyectojuegosmonolito.game.dto;

import jakarta.validation.constraints.NotBlank;

public record VideoUrlRequest(
    @NotBlank String videoUrl
) {}
