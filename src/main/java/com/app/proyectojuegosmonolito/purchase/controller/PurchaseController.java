package com.app.proyectojuegosmonolito.purchase.controller;

import com.app.proyectojuegosmonolito.purchase.dto.PurchaseRequest;
import com.app.proyectojuegosmonolito.purchase.dto.PurchaseResponse;
import com.app.proyectojuegosmonolito.purchase.mapper.PurchaseMapper;
import com.app.proyectojuegosmonolito.purchase.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Purchases", description = "Purchase management APIs")
@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final PurchaseMapper purchaseMapper;

    @Operation(summary = "Get all purchases", description = "Returns a paginated list of all purchases")
    @ApiResponse(responseCode = "200", description = "List of purchases retrieved successfully")
    @GetMapping
    public ResponseEntity<Page<PurchaseResponse>> findAll(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(purchaseService.findAll(pageable).map(purchaseMapper::toResponse));
    }

    @Operation(summary = "Get purchases by user", description = "Returns a paginated list of purchases for a specific user")
    @ApiResponse(responseCode = "200", description = "List of purchases retrieved successfully")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PurchaseResponse>> findByUserId(@PathVariable Long userId, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(purchaseService.findByUserId(userId, pageable).map(purchaseMapper::toResponse));
    }

    @Operation(summary = "Get purchase by ID", description = "Returns a single purchase by its ID")
    @ApiResponse(responseCode = "200", description = "Purchase found")
    @ApiResponse(responseCode = "404", description = "Purchase not found")
    @GetMapping("/{id}")
    public ResponseEntity<PurchaseResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseMapper.toResponse(purchaseService.findById(id)));
    }

    @Operation(summary = "Create a new purchase", description = "Creates a new purchase, deducts wallet balance, and adds games to library")
    @ApiResponse(responseCode = "201", description = "Purchase created successfully")
    @ApiResponse(responseCode = "400", description = "Insufficient balance or invalid request")
    @PostMapping
    public ResponseEntity<PurchaseResponse> create(@Valid @RequestBody PurchaseRequest request) {
        var purchase = purchaseService.create(request.userId(), request.items());
        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseMapper.toResponse(purchase));
    }
}
