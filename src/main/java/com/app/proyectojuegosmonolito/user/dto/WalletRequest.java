package com.app.proyectojuegosmonolito.user.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record WalletRequest(
    @NotNull BigDecimal balance
) {}
