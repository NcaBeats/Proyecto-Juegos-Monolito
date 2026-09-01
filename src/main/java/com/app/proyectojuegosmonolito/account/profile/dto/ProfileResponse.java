package com.app.proyectojuegosmonolito.account.profile.dto;

import com.app.proyectojuegosmonolito.account.profile.model.Visibility;
import java.time.Instant;

public record ProfileResponse(
    Long userId,
    String nickname,
    String avatarImage,
    String bio,
    Visibility visibility,
    Instant createdAt
) {}
