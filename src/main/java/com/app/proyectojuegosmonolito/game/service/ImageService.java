package com.app.proyectojuegosmonolito.game.service;

import com.app.proyectojuegosmonolito.game.dto.SignedImageUploadResponse;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ImageService {

    private final Cloudinary cloudinary;

    public ImageService(@Value("${app.cloudinary.url}") String url) {
        this.cloudinary = new Cloudinary(url);
    }

    @SuppressWarnings("unchecked")
    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        try {
            Map<String, Object> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("resource_type", "image")
            );
            String secureUrl = (String) result.get("secure_url");
            log.info("Uploaded image to Cloudinary: {} ({} bytes)", secureUrl, file.getSize());
            return secureUrl;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image to Cloudinary", e);
        }
    }

    @SuppressWarnings("unchecked")
    public String store(MultipartFile file, String folder) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        try {
            Map<String, Object> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "image",
                            "folder", folder
                    )
            );
            String secureUrl = (String) result.get("secure_url");
            log.info("Uploaded image to Cloudinary folder={}: {} ({} bytes)", folder, secureUrl, file.getSize());
            return secureUrl;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image to Cloudinary", e);
        }
    }

    @SuppressWarnings("unchecked")
    public void delete(String url) {
        if (url == null || url.isBlank()) {
            return;
        }
        try {
            String publicId = extractPublicId(url);
            Map<String, Object> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Deleted image from Cloudinary: {} (result={})", publicId, result.get("result"));
        } catch (IOException e) {
            log.warn("Failed to delete image from Cloudinary: {}", url, e);
        }
    }

    /**
     * Genera los parametros firmados que el navegador necesita para subir la imagen
     * directo a Cloudinary. La firma cubre timestamp y folder: si el cliente altera
     * alguno de los dos, Cloudinary rechaza la subida.
     */
    public SignedImageUploadResponse signUpload(String folder) {
        var config = cloudinary.config;
        if (config == null || config.cloudName == null || config.cloudName.isBlank()
                || config.apiKey == null || config.apiKey.isBlank()
                || config.apiSecret == null || config.apiSecret.isBlank()) {
            throw new IllegalStateException(
                    "CLOUDINARY_URL no esta configurada; no se pueden firmar subidas");
        }
        long timestamp = Instant.now().getEpochSecond();
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("timestamp", timestamp);
        params.put("folder", folder);
        String signature = signParameters(params, config.apiSecret);
        log.info("Signed Cloudinary upload folder={} expiresAt={}", folder, timestamp + 3600);
        return new SignedImageUploadResponse(
                "https://api.cloudinary.com/v1_1/" + config.cloudName + "/image/upload",
                config.cloudName,
                config.apiKey,
                timestamp,
                signature,
                folder
        );
    }

    /**
     * Algoritmo de firma de Cloudinary: params no vacios ordenados por clave,
     * unidos con &amp; como key=value, con el api_secret pegado al final, y SHA-1 en hex.
     */
    private String signParameters(Map<String, Object> params, String apiSecret) {
        var payload = params.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().toString().isBlank())
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&")) + apiSecret;
        try {
            return StringUtils.encodeHexString(MessageDigest.getInstance("SHA-1")
                    .digest(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 no disponible en esta JVM", e);
        }
    }

    private String extractPublicId(String url) {
        String path = url;
        int uploadIdx = path.indexOf("/upload/");
        if (uploadIdx >= 0) {
            path = path.substring(uploadIdx + "/upload/".length());
        }
        int versionIdx = path.indexOf('/');
        if (versionIdx >= 0) {
            path = path.substring(versionIdx + 1);
        }
        int dotIdx = path.lastIndexOf('.');
        if (dotIdx >= 0) {
            path = path.substring(0, dotIdx);
        }
        return path;
    }
}
