package com.app.proyectojuegosmonolito.user.dto;

import com.app.proyectojuegosmonolito.user.model.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProfileRequest(
    @NotBlank @Size(max = 255) String nickname,
    String avatarImage,
    String bio,
    @NotNull Visibility visibility
) {}
