package com.app.proyectojuegosmonolito.game.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

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
