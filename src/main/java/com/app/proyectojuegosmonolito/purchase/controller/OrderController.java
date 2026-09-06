package com.app.proyectojuegosmonolito.purchase.controller;

import com.app.proyectojuegosmonolito.SecurityContext;
import com.app.proyectojuegosmonolito.account.user.model.Role;
import com.app.proyectojuegosmonolito.account.user.service.UserService;
import com.app.proyectojuegosmonolito.purchase.dto.PurchaseResponse;
import com.app.proyectojuegosmonolito.purchase.mapper.PurchaseMapper;
import com.app.proyectojuegosmonolito.purchase.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Staff orders", description = "Order management APIs for ADMIN and VENDEDOR")
@RestController
@RequestMapping("/api/v1/manage/orders")
@RequiredArgsConstructor
public class OrderController {

    private final PurchaseService purchaseService;
    private final PurchaseMapper purchaseMapper;
    private final UserService userService;
    private final SecurityContext securityContext;

    @Operation(summary = "Staff order list", description = "ADMIN sees all orders; VENDEDOR sees only orders that include at least one of their games")
    @ApiResponse(responseCode = "200", description = "List of orders retrieved successfully")
    @GetMapping
    public ResponseEntity<Page<PurchaseResponse>> findAll(@ParameterObject Pageable pageable) {
        var user = userService.findById(securityContext.getCurrentUserId());
        if (user.getRole() == Role.ADMIN) {
            return ResponseEntity.ok(purchaseService.findAll(pageable).map(purchaseMapper::toResponse));
        }
        return ResponseEntity.ok(purchaseService.findBySellerId(user.getId(), pageable).map(purchaseMapper::toResponse));
    }

    @Operation(summary = "Staff order detail", description = "ADMIN can access any order; VENDEDOR only orders containing their games")
    @ApiResponse(responseCode = "200", description = "Order found")
    @ApiResponse(responseCode = "404", description = "Order not found or not owned")
    @GetMapping("/{id}")
    public ResponseEntity<PurchaseResponse> findById(@PathVariable Long id) {
        var user = userService.findById(securityContext.getCurrentUserId());
        var purchase = purchaseService.findById(id);
        boolean allowed = user.getRole() == Role.ADMIN
                || purchaseService.isSellerOrder(id, user.getId());
        if (!allowed) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(purchaseMapper.toResponse(purchase));
    }
}