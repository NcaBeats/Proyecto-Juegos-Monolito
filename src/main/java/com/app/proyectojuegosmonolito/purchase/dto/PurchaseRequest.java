package com.app.proyectojuegosmonolito.purchase.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PurchaseRequest(
    @NotNull Long userId,
    @NotEmpty List<PurchaseItemRequest> items
) {}
