package com.app.proyectojuegosmonolito.library.controller;

import com.app.proyectojuegosmonolito.library.dto.LibraryRequest;
import com.app.proyectojuegosmonolito.library.dto.LibraryResponse;
import com.app.proyectojuegosmonolito.library.mapper.LibraryMapper;
import com.app.proyectojuegosmonolito.library.service.LibraryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/{userId}/library")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;
    private final LibraryMapper libraryMapper;

    @GetMapping
    public ResponseEntity<Page<LibraryResponse>> findByUserId(@PathVariable Long userId, Pageable pageable) {
        return ResponseEntity.ok(libraryService.findByUserId(userId, pageable).map(libraryMapper::toResponse));
    }

    @PostMapping
    public ResponseEntity<LibraryResponse> add(@Valid @RequestBody LibraryRequest request) {
        var library = libraryService.add(request.userId(), request.gameId());
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryMapper.toResponse(library));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@PathVariable Long id) {
        libraryService.remove(id);
        return ResponseEntity.noContent().build();
    }
}
