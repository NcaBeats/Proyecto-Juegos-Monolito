package com.app.proyectojuegosmonolito.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Solicita una URL firmada para subir la portada de un blog directo a R2.
 * El backend deriva la key (blogs/{slug}/cover/{uuid}) del titulo del post;
 * el cliente nunca elige la ruta. El contentType queda firmado y debe
 * reenviarse identico en el PUT.
 */
public record BlogCoverPresignRequest(
        @NotBlank String title,
        @NotBlank @Pattern(regexp = "(?i)image/(png|jpe?g|webp|avif|gif)") String contentType
) {
}
