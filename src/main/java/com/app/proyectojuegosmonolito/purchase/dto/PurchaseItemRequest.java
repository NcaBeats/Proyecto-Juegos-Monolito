package com.app.proyectojuegosmonolito.purchase.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PurchaseItemRequest(
    @NotNull Long gameId,
    @NotNull @Min(1) Integer quantity
) {}
