package com.app.proyectojuegosmonolito.library.dto;

import java.time.Instant;

public record LibraryResponse(
    Long id,
    Long userId,
    Long gameId,
    Instant acquiredAt
) {}
