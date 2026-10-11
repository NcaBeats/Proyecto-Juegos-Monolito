package com.app.proyectojuegosmonolito.blog.dto;

import com.app.proyectojuegosmonolito.blog.model.BlogCategory;

import java.time.Instant;

public record BlogResponse(
        Long id,
        String title,
        String excerpt,
        String content,
        String coverImage,
        BlogCategory category,
        Instant publishedAt,
        Instant createdAt
) {}
