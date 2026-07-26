package com.app.proyectojuegosmonolito.user.dto;

import com.app.proyectojuegosmonolito.user.model.Visibility;
import java.time.Instant;

public record ProfileResponse(
    Long userId,
    String nickname,
    String avatarImage,
    String bio,
    Visibility visibility,
    Instant createdAt
) {}
