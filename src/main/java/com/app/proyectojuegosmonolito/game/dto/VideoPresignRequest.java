package com.app.proyectojuegosmonolito.game.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Solicita una URL firmada para subir el trailer directo a R2.
 * El contentType viaja en la firma: el cliente debe reenviarlo identico en el PUT.
 *
 * @param fingerprint huella del contenido calculada por el cliente. Es opcional a
 *                    proposito: si falta se usa la clave canonica {slug}/trailer.mp4,
 *                    de modo que un bundle viejo del frontend que no la envie siga
 *                    pudiendo subir, solo que sin el beneficio del cache inmutable.
 */
public record VideoPresignRequest(
        @NotBlank String name,
        @NotBlank String contentType,
        @Pattern(regexp = "^[0-9a-f]{8}$", message = "fingerprint debe ser 8 caracteres hexadecimales")
        String fingerprint
) {
}
