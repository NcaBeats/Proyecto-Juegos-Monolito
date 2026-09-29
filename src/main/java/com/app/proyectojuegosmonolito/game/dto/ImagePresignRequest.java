package com.app.proyectojuegosmonolito.game.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Solicita los parametros firmados para subir una imagen directo a Cloudinary.
 * El backend deriva la carpeta del nombre del juego; el cliente nunca elige la ruta.
 */
public record ImagePresignRequest(
        @NotBlank String name,
        @NotBlank @Pattern(regexp = "(?i)image|banner|gallery") String kind
) {
}
