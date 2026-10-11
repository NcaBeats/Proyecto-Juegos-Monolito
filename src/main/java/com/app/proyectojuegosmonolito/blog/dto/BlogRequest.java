package com.app.proyectojuegosmonolito.blog.dto;

import com.app.proyectojuegosmonolito.blog.model.BlogCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BlogRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 500) String excerpt,
        @NotBlank String content,
        @Size(max = 500) String coverImage,
        BlogCategory category
) {}
