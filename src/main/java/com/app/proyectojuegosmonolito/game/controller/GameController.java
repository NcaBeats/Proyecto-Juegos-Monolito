package com.app.proyectojuegosmonolito.game.controller;

import com.app.proyectojuegosmonolito.game.dto.GameRequest;
import com.app.proyectojuegosmonolito.game.dto.GameResponse;
import com.app.proyectojuegosmonolito.game.mapper.GameMapper;
import com.app.proyectojuegosmonolito.game.model.Category;
import com.app.proyectojuegosmonolito.game.service.CategoryService;
import com.app.proyectojuegosmonolito.game.service.GameService;
import com.app.proyectojuegosmonolito.game.service.ImageService;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "Games", description = "Game management APIs")
@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final GameMapper gameMapper;
    private final CategoryService categoryService;
    private final ImageService imageService;

    @Operation(summary = "Get all games", description = "Returns a paginated list of all games")
    @ApiResponse(responseCode = "200", description = "List of games retrieved successfully")
    @GetMapping
    public ResponseEntity<Page<GameResponse>> findAll(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(gameService.findAll(pageable).map(gameMapper::toResponse));
    }

    @Operation(summary = "Get discounted games", description = "Returns a paginated list of games with active discounts")
    @ApiResponse(responseCode = "200", description = "List of discounted games retrieved successfully")
    @GetMapping("/discounted")
    public ResponseEntity<Page<GameResponse>> findDiscounted(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(gameService.findDiscounted(pageable).map(gameMapper::toResponse));
    }

    @Operation(summary = "Get banner games", description = "Returns games that have a banner image")
    @ApiResponse(responseCode = "200", description = "List of banner games retrieved successfully")
    @GetMapping("/banners")
    public ResponseEntity<Page<GameResponse>> findBanners(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(gameService.findBannerGames(pageable).map(gameMapper::toResponse));
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
        var game = gameMapper.toEntity(request);
        var categories = resolveCategories(request.categoryNames());
        game.setCategories(categories);
        var saved = gameService.create(game);
        return ResponseEntity.status(HttpStatus.CREATED).body(gameMapper.toResponse(saved));
    }

    @Operation(summary = "Update a game", description = "Updates an existing game by its ID")
    @ApiResponse(responseCode = "200", description = "Game updated successfully")
    @ApiResponse(responseCode = "404", description = "Game not found")
    @PutMapping("/{id}")
    public ResponseEntity<GameResponse> update(@PathVariable Long id, @Valid @RequestBody GameRequest request) {
        var categories = resolveCategories(request.categoryNames());
        var game = gameService.update(id, request.name(), request.originalPrice(), request.discountPercent(),
                request.description(), request.state(), request.launchDate(), categories);
        return ResponseEntity.ok(gameMapper.toResponse(game));
    }

    @Operation(summary = "Upload game cover image", description = "Uploads an image for the specified game")
    @ApiResponse(responseCode = "200", description = "Image uploaded successfully")
    @ApiResponse(responseCode = "404", description = "Game not found")
    @PostMapping("/{id}/image")
    public ResponseEntity<GameResponse> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        var imageUrl = imageService.store(file);
        var game = gameService.updateImage(id, imageUrl);
        return ResponseEntity.ok(gameMapper.toResponse(game));
    }

    @Operation(summary = "Upload game banner image", description = "Uploads a banner image for the specified game")
    @ApiResponse(responseCode = "200", description = "Banner uploaded successfully")
    @ApiResponse(responseCode = "404", description = "Game not found")
    @PostMapping("/{id}/banner")
    public ResponseEntity<GameResponse> uploadBanner(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        var bannerUrl = imageService.store(file);
        var game = gameService.updateBannerUrl(id, bannerUrl);
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

    private List<Category> resolveCategories(List<String> categoryNames) {
        if (categoryNames == null || categoryNames.isEmpty()) {
            return new ArrayList<>();
        }
        return categoryNames.stream()
                .map(name -> {
                    var existing = categoryService.findAll(Pageable.unpaged()).stream()
                            .filter(c -> c.getName().equals(name))
                            .findFirst();
                    return existing.orElseGet(() -> categoryService.create(
                            Category.builder().name(name).build()));
                })
                .toList();
    }
}
