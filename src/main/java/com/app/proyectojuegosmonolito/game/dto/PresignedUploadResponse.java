package com.app.proyectojuegosmonolito.game.dto;

/**
 * Credenciales de corta duracion para que el navegador suba el trailer
 * directamente a Cloudflare R2, sin pasar por el servidor que sirve el frontend.
 *
 * @param uploadUrl  URL firmada; el navegador debe hacer PUT contra esta URL
 * @param key        clave dentro del bucket (slug/trailer.mp4)
 * @param publicPath ruta relativa que se persiste en la entidad Game
 * @param contentType valor exacto que el navegador debe enviar en el header Content-Type;
 *                    forma parte de la firma, un valor distinto produce 403 SignatureDoesNotMatch
 * @param expiresInSeconds vigencia de la firma
 */
public record PresignedUploadResponse(
        String uploadUrl,
        String key,
        String publicPath,
        String contentType,
        long expiresInSeconds
) {
}
