package com.app.proyectojuegosmonolito.purchase.dto;

import java.math.BigDecimal;

public record PurchaseItemResponse(
    Long id,
    Long gameId,
    String gameName,
    BigDecimal unitPrice,
    Integer quantity,
    BigDecimal subtotal
) {}
