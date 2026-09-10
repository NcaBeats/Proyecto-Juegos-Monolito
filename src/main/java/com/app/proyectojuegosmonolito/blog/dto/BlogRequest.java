package com.app.proyectojuegosmonolito.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BlogRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 500) String excerpt,
        @NotBlank String content,
        @Size(max = 500) String coverImage,
        @Size(max = 50) String category
) {}
