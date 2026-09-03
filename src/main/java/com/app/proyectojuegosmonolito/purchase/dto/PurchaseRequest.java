package com.app.proyectojuegosmonolito.purchase.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record PurchaseRequest(
    @NotEmpty List<PurchaseItemRequest> items
) {}
