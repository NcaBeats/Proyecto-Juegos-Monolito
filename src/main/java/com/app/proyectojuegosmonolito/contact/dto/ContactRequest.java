package com.app.proyectojuegosmonolito.contact.dto;

import com.app.proyectojuegosmonolito.validation.ValidEmailDomain;
import jakarta.validation.constraints.*;

public record ContactRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 100) @ValidEmailDomain String email,
        @NotBlank @Size(max = 500) String comment
) {}
