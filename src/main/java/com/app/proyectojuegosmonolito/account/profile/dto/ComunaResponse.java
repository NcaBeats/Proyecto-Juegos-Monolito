package com.app.proyectojuegosmonolito.account.profile.dto;

import java.util.List;

public record ComunaResponse(
        Long id,
        String nombre,
        Long regionId
) {}