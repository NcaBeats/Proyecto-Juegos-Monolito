package com.app.proyectojuegosmonolito.contact.controller;

import com.app.proyectojuegosmonolito.contact.dto.ContactRequest;
import com.app.proyectojuegosmonolito.contact.dto.ContactResponse;
import com.app.proyectojuegosmonolito.contact.mapper.ContactMapper;
import com.app.proyectojuegosmonolito.contact.service.ContactService;
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

@Tag(name = "Contacts", description = "Contact form APIs")
@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;
    private final ContactMapper contactMapper;

    @Operation(summary = "Send a contact message", description = "Public endpoint. Anyone can send a contact message.")
    @ApiResponse(responseCode = "201", description = "Contact message sent successfully")
    @PostMapping
    public ResponseEntity<ContactResponse> create(@Valid @RequestBody ContactRequest request) {
        var contact = contactMapper.toEntity(request);
        var saved = contactService.create(contact);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contactMapper.toResponse(saved));
    }

    @Operation(summary = "Get all contacts", description = "Returns a paginated list of all contact messages (ADMIN only)")
    @ApiResponse(responseCode = "200", description = "List of contacts retrieved successfully")
    @GetMapping
    public ResponseEntity<Page<ContactResponse>> findAll(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(contactService.findAll(pageable).map(contactMapper::toResponse));
    }
}
