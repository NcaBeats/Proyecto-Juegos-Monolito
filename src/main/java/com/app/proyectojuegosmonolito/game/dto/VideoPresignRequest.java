package com.app.proyectojuegosmonolito.game.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Solicita una URL firmada para subir el trailer directo a R2.
 * El contentType viaja en la firma: el cliente debe reenviarlo identico en el PUT.
 */
public record VideoPresignRequest(
        @NotBlank String name,
        @NotBlank String contentType
) {
}
