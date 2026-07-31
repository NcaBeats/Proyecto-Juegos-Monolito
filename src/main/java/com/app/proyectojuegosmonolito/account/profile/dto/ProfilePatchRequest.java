package com.app.proyectojuegosmonolito.account.profile.dto;

import com.app.proyectojuegosmonolito.account.profile.model.Visibility;
import jakarta.validation.constraints.Size;

public record ProfilePatchRequest(
    @Size(max = 255) String nickname,
    String bio,
    Visibility visibility
) {}
