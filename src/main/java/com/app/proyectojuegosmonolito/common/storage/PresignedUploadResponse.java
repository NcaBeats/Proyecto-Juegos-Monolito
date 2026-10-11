package com.app.proyectojuegosmonolito.common.storage;

/**
 * Credenciales de corta duracion para que el navegador suba un archivo
 * directamente a Cloudflare R2, sin pasar por el servidor que sirve el frontend.
 *
 * @param uploadUrl  URL firmada; el navegador debe hacer PUT contra esta URL
 * @param key        clave dentro del bucket (p.ej. {slug}/trailer.mp4 o blogs/{slug}/cover/{uuid}.webp)
 * @param publicPath URL publica del objeto en el bucket; es lo que el cliente persiste
 *                    en la entidad. El nombre del campo es historico: es una URL
 *                    absoluta, no una ruta. Todos los campos de media siguen
 *                    esta misma forma (ver MediaUrl.requireAbsolute).
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
