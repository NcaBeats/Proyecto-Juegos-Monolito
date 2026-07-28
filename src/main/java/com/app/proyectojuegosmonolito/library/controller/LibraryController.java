package com.app.proyectojuegosmonolito.library.controller;

import com.app.proyectojuegosmonolito.library.dto.LibraryRequest;
import com.app.proyectojuegosmonolito.library.dto.LibraryResponse;
import com.app.proyectojuegosmonolito.library.mapper.LibraryMapper;
import com.app.proyectojuegosmonolito.library.service.LibraryService;
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

@Tag(name = "Library", description = "Library management APIs")
@RestController
@RequestMapping("/api/v1/users/{userId}/library")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;
    private final LibraryMapper libraryMapper;

    @Operation(summary = "Get library by user", description = "Returns a paginated list of games in a user's library")
    @ApiResponse(responseCode = "200", description = "Library retrieved successfully")
    @GetMapping
    public ResponseEntity<Page<LibraryResponse>> findByUserId(@PathVariable Long userId, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(libraryService.findByUserId(userId, pageable).map(libraryMapper::toResponse));
    }

    @Operation(summary = "Add game to library", description = "Adds a game to a user's library")
    @ApiResponse(responseCode = "201", description = "Game added to library successfully")
    @ApiResponse(responseCode = "400", description = "Game already in library")
    @PostMapping
    public ResponseEntity<LibraryResponse> add(@PathVariable Long userId, @Valid @RequestBody LibraryRequest request) {
        var library = libraryService.add(userId, request.gameId());
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryMapper.toResponse(library));
    }

    @Operation(summary = "Remove from library", description = "Removes a game entry from a user's library")
    @ApiResponse(responseCode = "204", description = "Game removed from library successfully")
    @ApiResponse(responseCode = "404", description = "Library entry not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@PathVariable Long userId, @PathVariable Long id) {
        libraryService.remove(id, userId);
        return ResponseEntity.noContent().build();
    }
}
