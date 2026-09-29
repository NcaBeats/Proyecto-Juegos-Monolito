package com.app.proyectojuegosmonolito.blog.dto;

import java.time.Instant;

public record BlogResponse(
        Long id,
        String title,
        String excerpt,
        String content,
        String coverImage,
        String category,
        Instant publishedAt,
        Instant createdAt
) {}
