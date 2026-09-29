package com.app.proyectojuegosmonolito.game.controller;

import com.app.proyectojuegosmonolito.SecurityContext;
import com.app.proyectojuegosmonolito.account.user.model.Role;
import com.app.proyectojuegosmonolito.account.user.model.User;
import com.app.proyectojuegosmonolito.account.user.service.UserService;
import com.app.proyectojuegosmonolito.game.dto.GameRequest;
import com.app.proyectojuegosmonolito.game.dto.GameResponse;
import com.app.proyectojuegosmonolito.game.dto.GameStatsResponse;
import com.app.proyectojuegosmonolito.game.dto.VideoUrlRequest;
import com.app.proyectojuegosmonolito.game.mapper.GameMapper;
import com.app.proyectojuegosmonolito.game.model.Category;
import com.app.proyectojuegosmonolito.game.model.GameState;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private final UserService userService;
    private final SecurityContext securityContext;

    @Operation(summary = "Get all games", description = "Returns a paginated list of all games, optionally filtered by category or name")
    @ApiResponse(responseCode = "200", description = "List of games retrieved successfully")
    @GetMapping
    public ResponseEntity<Page<GameResponse>> findAll(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String name,
            @ParameterObject Pageable pageable) {
        if (name != null && !name.isBlank()) {
            return ResponseEntity.ok(gameService.findByName(name, pageable).map(gameMapper::toResponse));
        }
        if (category != null && !category.isBlank()) {
            return ResponseEntity.ok(gameService.findByCategory(category, pageable).map(gameMapper::toResponse));
        }
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

    @Operation(summary = "Get game stats", description = "Returns counts and catalog value for all or a given game state")
    @ApiResponse(responseCode = "200", description = "Stats retrieved successfully")
    @GetMapping("/stats")
    public ResponseEntity<GameStatsResponse> stats(
            @RequestParam(defaultValue = "AVAILABLE") GameState state) {
        var total = gameService.count();
        var active = gameService.countByState(state);
        var catalogValue = gameService.sumDiscountedPriceByState(state);
        return ResponseEntity.ok(new GameStatsResponse(total, active, catalogValue));
    }

    @Operation(summary = "Get game by ID", description = "Returns a single game by its ID")
    @ApiResponse(responseCode = "200", description = "Game found")
    @ApiResponse(responseCode = "404", description = "Game not found")
    @GetMapping("/{id}")
    public ResponseEntity<GameResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(gameMapper.toResponse(gameService.findById(id)));
    }

    @Operation(summary = "Create a new game", description = "Creates a new game with the provided details and optional media files. " +
            "Parts: metadata (JSON), image (required), banner, video, gallery (multiple)")
    @ApiResponse(responseCode = "201", description = "Game created successfully")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GameResponse> create(
            @RequestPart("metadata") @Valid GameRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "banner", required = false) MultipartFile banner,
            @RequestPart(value = "video", required = false) MultipartFile video,
            @RequestPart(value = "gallery", required = false) List<MultipartFile> gallery) throws IOException {
        var user = userService.findById(securityContext.getCurrentUserId());
        User seller = resolveSellerForCreate(request.sellerId(), user);
        var categories = resolveCategories(request.categoryNames());
        var saved = gameService.createWithFiles(request, categories, seller, image, banner, video, gallery);
        return ResponseEntity.status(HttpStatus.CREATED).body(gameMapper.toResponse(saved));
    }

    @Operation(summary = "Update a game", description = "Updates an existing game and optional media files by its ID. " +
            "VENDEDOR can only update games assigned to them; a NEW image is required on create but optional here.")
    @ApiResponse(responseCode = "200", description = "Game updated successfully")
    @ApiResponse(responseCode = "404", description = "Game not found")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GameResponse> update(
            @PathVariable Long id,
            @RequestPart("metadata") @Valid GameRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "banner", required = false) MultipartFile banner,
            @RequestPart(value = "video", required = false) MultipartFile video,
            @RequestPart(value = "gallery", required = false) List<MultipartFile> gallery) throws IOException {
        assertCanModify(id);
        var categories = resolveCategories(request.categoryNames());
        var game = gameService.updateWithFiles(id, request, categories, image, banner, video, gallery);
        return ResponseEntity.ok(gameMapper.toResponse(game));
    }

    @Operation(summary = "Upload game cover image", description = "Uploads an image for the specified game")
    @ApiResponse(responseCode = "200", description = "Image uploaded successfully")
    @ApiResponse(responseCode = "404", description = "Game not found")
    @PostMapping("/{id}/image")
    public ResponseEntity<GameResponse> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws java.io.IOException {
        assertCanModify(id);
        var imageUrl = imageService.store(file, "games/" + gameService.slugify(gameService.findById(id).getName()) + "/card");
        var game = gameService.updateImage(id, imageUrl);
        return ResponseEntity.ok(gameMapper.toResponse(game));
    }

    @Operation(summary = "Upload game banner image", description = "Uploads a banner image for the specified game")
    @ApiResponse(responseCode = "200", description = "Banner uploaded successfully")
    @ApiResponse(responseCode = "404", description = "Game not found")
    @PostMapping("/{id}/banner")
    public ResponseEntity<GameResponse> uploadBanner(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws java.io.IOException {
        assertCanModify(id);
        var bannerUrl = imageService.store(file, "games/" + gameService.slugify(gameService.findById(id).getName()) + "/banner");
        var game = gameService.updateBannerUrl(id, bannerUrl);
        return ResponseEntity.ok(gameMapper.toResponse(game));
    }

    @Operation(summary = "Set game video URL", description = "Sets the video (trailer) URL for the specified game")
    @ApiResponse(responseCode = "200", description = "Video updated successfully")
    @ApiResponse(responseCode = "404", description = "Game not found")
    @PutMapping("/{id}/video")
    public ResponseEntity<GameResponse> uploadVideo(
            @PathVariable Long id,
            @Valid @RequestBody VideoUrlRequest request) {
        var game = gameService.updateVideoUrl(id, request.videoUrl());
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

    private User resolveSellerForCreate(Long requestedSellerId, User currentUser) {
        if (currentUser.getRole() == Role.VENDEDOR) {
            return currentUser;
        }
        if (requestedSellerId != null) {
            return userService.findById(requestedSellerId);
        }
        return null;
    }

    private void assertCanModify(Long id) {
        var user = userService.findById(securityContext.getCurrentUserId());
        gameService.assertCanModify(gameService.findById(id), user);
    }
}
