package com.app.proyectojuegosmonolito.account.profile.dto;

import com.app.proyectojuegosmonolito.account.profile.model.Region;
import com.app.proyectojuegosmonolito.account.profile.model.Visibility;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProfilePatchRequest(
        @Size(max = 255) String nickname,
        String bio,
        Visibility visibility,
        @Size(max = 50) String firstName,
        @Size(max = 100) String lastName,
        LocalDate birthDate,
        Region region,
        @Size(max = 100) String comuna,
        @Size(max = 300) String address
) {}
