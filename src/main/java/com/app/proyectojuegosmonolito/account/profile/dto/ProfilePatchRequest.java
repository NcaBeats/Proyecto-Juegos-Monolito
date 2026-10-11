package com.app.proyectojuegosmonolito.account.profile.dto;

import com.app.proyectojuegosmonolito.account.profile.model.ValidRun;
import com.app.proyectojuegosmonolito.account.profile.model.Visibility;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProfilePatchRequest(
        @ValidRun String run,
        @Size(max = 255) String nickname,
        String bio,
        Visibility visibility,
        @Size(max = 50) String firstName,
        @Size(max = 100) String lastName,
        LocalDate birthDate,
        Long comunaId,
        @Size(max = 300) String address
) {}
