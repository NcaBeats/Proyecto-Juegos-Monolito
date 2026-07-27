package com.app.proyectojuegosmonolito.game.controller;

import com.app.proyectojuegosmonolito.game.dto.GameRequest;
import com.app.proyectojuegosmonolito.game.dto.GameResponse;
import com.app.proyectojuegosmonolito.game.mapper.GameMapper;
import com.app.proyectojuegosmonolito.game.service.GameService;
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

@Tag(name = "Games", description = "Game management APIs")
@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final GameMapper gameMapper;

    @Operation(summary = "Get all games", description = "Returns a paginated list of all games")
    @ApiResponse(responseCode = "200", description = "List of games retrieved successfully")
    @GetMapping
    public ResponseEntity<Page<GameResponse>> findAll(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(gameService.findAll(pageable).map(gameMapper::toResponse));
    }

    @Operation(summary = "Get game by ID", description = "Returns a single game by its ID")
    @ApiResponse(responseCode = "200", description = "Game found")
    @ApiResponse(responseCode = "404", description = "Game not found")
    @GetMapping("/{id}")
    public ResponseEntity<GameResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(gameMapper.toResponse(gameService.findById(id)));
    }

    @Operation(summary = "Create a new game", description = "Creates a new game with the provided details")
    @ApiResponse(responseCode = "201", description = "Game created successfully")
    @PostMapping
    public ResponseEntity<GameResponse> create(@Valid @RequestBody GameRequest request) {
        var game = gameService.create(gameMapper.toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(gameMapper.toResponse(game));
    }

    @Operation(summary = "Update a game", description = "Updates an existing game by its ID")
    @ApiResponse(responseCode = "200", description = "Game updated successfully")
    @ApiResponse(responseCode = "404", description = "Game not found")
    @PutMapping("/{id}")
    public ResponseEntity<GameResponse> update(@PathVariable Long id, @Valid @RequestBody GameRequest request) {
        var game = gameService.update(id, request.name(), request.price(), request.description(), request.state(), request.launchDate());
        return ResponseEntity.ok(gameMapper.toResponse(game));
    }

    @Operation(summary = "Delete a game", description = "Deletes a game by its ID")
    @ApiResponse(responseCode = "204", description = "Game deleted successfully")
    @ApiResponse(responseCode = "404", description = "Game not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        gameService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
