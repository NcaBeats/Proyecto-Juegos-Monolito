package com.app.proyectojuegosmonolito.library.dto;

import jakarta.validation.constraints.NotNull;

public record LibraryRequest(
    @NotNull Long gameId
) {}
