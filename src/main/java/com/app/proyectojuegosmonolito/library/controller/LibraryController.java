package com.app.proyectojuegosmonolito.library.controller;

import com.app.proyectojuegosmonolito.library.dto.LibraryRequest;
import com.app.proyectojuegosmonolito.library.dto.LibraryResponse;
import com.app.proyectojuegosmonolito.library.mapper.LibraryMapper;
import com.app.proyectojuegosmonolito.library.service.LibraryService;
import com.app.proyectojuegosmonolito.SecurityContext;
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
@RequestMapping("/api/v1/library")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;
    private final LibraryMapper libraryMapper;
    private final SecurityContext securityContext;

    @Operation(summary = "Get my library", description = "Returns a paginated list of games in the authenticated user's library")
    @ApiResponse(responseCode = "200", description = "Library retrieved successfully")
    @GetMapping
    public ResponseEntity<Page<LibraryResponse>> findMyLibrary(@ParameterObject Pageable pageable) {
        var userId = securityContext.getCurrentUserId();
        return ResponseEntity.ok(libraryService.findByUserId(userId, pageable).map(libraryMapper::toResponse));
    }

    @Operation(summary = "Add game to my library", description = "Adds a game to the authenticated user's library")
    @ApiResponse(responseCode = "201", description = "Game added to library successfully")
    @ApiResponse(responseCode = "400", description = "Game already in library")
    @PostMapping
    public ResponseEntity<LibraryResponse> add(@Valid @RequestBody LibraryRequest request) {
        var userId = securityContext.getCurrentUserId();
        var library = libraryService.add(userId, request.gameId());
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryMapper.toResponse(library));
    }

    @Operation(summary = "Remove game by game ID", description = "Removes a game from the authenticated user's library by game ID")
    @ApiResponse(responseCode = "204", description = "Game removed from library successfully")
    @ApiResponse(responseCode = "404", description = "Game not found in library")
    @DeleteMapping("/game/{gameId}")
    public ResponseEntity<Void> removeByGame(@PathVariable Long gameId) {
        var userId = securityContext.getCurrentUserId();
        libraryService.removeByGame(userId, gameId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Clear library", description = "Removes all games from the authenticated user's library")
    @ApiResponse(responseCode = "204", description = "Library cleared successfully")
    @DeleteMapping
    public ResponseEntity<Void> clear() {
        var userId = securityContext.getCurrentUserId();
        libraryService.clear(userId);
        return ResponseEntity.noContent().build();
    }
}
