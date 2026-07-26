package com.app.proyectojuegosmonolito.purchase.mapper;

import com.app.proyectojuegosmonolito.purchase.model.Purchase;
import com.app.proyectojuegosmonolito.purchase.dto.PurchaseItemResponse;
import com.app.proyectojuegosmonolito.purchase.dto.PurchaseResponse;
import org.springframework.stereotype.Component;

@Component
public class PurchaseMapper {

    public PurchaseResponse toResponse(Purchase purchase) {
        return new PurchaseResponse(
                purchase.getId(),
                purchase.getUser().getId(),
                purchase.getTotalAmount(),
                purchase.getStatus(),
                purchase.getPurchasedAt(),
                purchase.getItems().stream()
                        .map(item -> new PurchaseItemResponse(
                                item.getId(),
                                item.getGame().getId(),
                                item.getGame().getName(),
                                item.getUnitPrice(),
                                item.getQuantity(),
                                item.getSubtotal()
                        ))
                        .toList()
        );
    }
}
