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
}
