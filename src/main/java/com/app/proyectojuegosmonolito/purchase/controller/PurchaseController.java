package com.app.proyectojuegosmonolito.purchase.controller;

import com.app.proyectojuegosmonolito.purchase.dto.PurchaseRequest;
import com.app.proyectojuegosmonolito.purchase.dto.PurchaseResponse;
import com.app.proyectojuegosmonolito.purchase.mapper.PurchaseMapper;
import com.app.proyectojuegosmonolito.purchase.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final PurchaseMapper purchaseMapper;

    @GetMapping
    public ResponseEntity<Page<PurchaseResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(purchaseService.findAll(pageable).map(purchaseMapper::toResponse));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PurchaseResponse>> findByUserId(@PathVariable Long userId, Pageable pageable) {
        return ResponseEntity.ok(purchaseService.findByUserId(userId, pageable).map(purchaseMapper::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseMapper.toResponse(purchaseService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<PurchaseResponse> create(@Valid @RequestBody PurchaseRequest request) {
        var purchase = purchaseService.create(request.userId(), request.items());
        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseMapper.toResponse(purchase));
    }
}
