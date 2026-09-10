package com.app.proyectojuegosmonolito.account.user.dto;

import com.app.proyectojuegosmonolito.account.profile.model.Comuna;
import com.app.proyectojuegosmonolito.account.profile.model.Region;
import com.app.proyectojuegosmonolito.account.profile.model.ValidRun;
import com.app.proyectojuegosmonolito.validation.ValidEmailDomain;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record UserRequestCreate(
    @NotBlank @Email @Size(max = 100) @ValidEmailDomain String email,
    @NotBlank @Size(min = 4, max = 10) String password,
    @NotBlank @Size(max = 50) String firstName,
    @NotBlank @Size(max = 100) String lastName,
    @NotBlank @Size(min = 7, max = 9) @ValidRun String run,
    LocalDate birthDate,
    @NotNull Region region,
    @NotNull Comuna comuna,
    @NotBlank @Size(max = 300) String address
) {}