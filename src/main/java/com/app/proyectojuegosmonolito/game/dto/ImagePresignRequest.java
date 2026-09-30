package com.app.proyectojuegosmonolito.game.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Solicita una URL firmada para subir una imagen directo a R2.
 * El backend deriva la key (slug + kind) del nombre del juego; el cliente nunca
 * elige la ruta. El contentType queda firmado y debe reenviarse identico en el PUT.
 */
public record ImagePresignRequest(
        @NotBlank String name,
        @NotBlank @Pattern(regexp = "(?i)image|banner|gallery") String kind,
        @NotBlank @Pattern(regexp = "(?i)image/(png|jpe?g|webp|avif|gif)") String contentType
) {
}
