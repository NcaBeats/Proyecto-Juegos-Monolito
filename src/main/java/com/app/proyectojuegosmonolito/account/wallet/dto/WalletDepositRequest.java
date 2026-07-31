package com.app.proyectojuegosmonolito.account.wallet.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record WalletDepositRequest(
    @NotNull @Positive BigDecimal amount
) {}
