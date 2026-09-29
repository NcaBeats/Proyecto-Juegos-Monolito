package com.app.proyectojuegosmonolito.game.dto;

/**
 * Parametros firmados por el backend para que el navegador suba la imagen
 * directo a la API de Cloudinary, sin pasar por el servidor que sirve el frontend.
 *
 * @param uploadUrl  endpoint de subida de Cloudinary ({cloudName}/image/upload)
 * @param cloudName  nombre del cloud de destino
 * @param apiKey     API key publica, no secreta
 * @param timestamp  segundos desde epoch; forma parte de la firma
 * @param signature  SHA-1 de los params + api_secret
 * @param folder     carpeta de destino; ya viene sanitizada por el backend
 */
public record SignedImageUploadResponse(
        String uploadUrl,
        String cloudName,
        String apiKey,
        long timestamp,
        String signature,
        String folder
) {
}
