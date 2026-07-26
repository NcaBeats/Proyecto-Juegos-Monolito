package com.app.proyectojuegosmonolito.purchase.dto;

import com.app.proyectojuegosmonolito.purchase.model.PurchaseStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PurchaseResponse(
    Long id,
    Long userId,
    BigDecimal totalAmount,
    PurchaseStatus status,
    Instant purchasedAt,
    List<PurchaseItemResponse> items
) {}
