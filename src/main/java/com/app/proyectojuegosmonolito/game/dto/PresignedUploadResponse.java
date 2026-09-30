package com.app.proyectojuegosmonolito.game.dto;

/**
 * Credenciales de corta duracion para que el navegador suba la media
 * directamente a Cloudflare R2, sin pasar por el servidor que sirve el frontend.
 *
 * @param uploadUrl  URL firmada; el navegador debe hacer PUT contra esta URL
 * @param key        clave dentro del bucket (p.ej. slug/trailer-a1b2c3d4.mp4)
 * @param publicPath ruta que se persiste en la entidad Game
 * @param contentType valor exacto que el navegador debe enviar en el header Content-Type;
 *                    forma parte de la firma, un valor distinto produce 403 SignatureDoesNotMatch
 * @param cacheControl valor exacto que el navegador debe enviar en el header Cache-Control;
 *                     tambien forma parte de la firma, y R2 solo guarda el header si el
 *                     servidor lo firmo. Viaja en la respuesta para que el cliente no
 *                     tenga su propia constante, que es donde se cuelan los 403 en silencio
 * @param expiresInSeconds vigencia de la firma
 */
public record PresignedUploadResponse(
        String uploadUrl,
        String key,
        String publicPath,
        String contentType,
        String cacheControl,
        long expiresInSeconds
) {
}
