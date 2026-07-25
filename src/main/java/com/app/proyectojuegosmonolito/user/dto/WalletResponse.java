package com.app.proyectojuegosmonolito.user.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record WalletResponse(
    Long userId,
    BigDecimal balance,
    Instant updatedAt
) {}
