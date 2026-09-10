package com.app.proyectojuegosmonolito.game.controller;

import com.app.proyectojuegosmonolito.SecurityContext;
import com.app.proyectojuegosmonolito.account.user.model.Role;
import com.app.proyectojuegosmonolito.account.user.service.UserService;
import com.app.proyectojuegosmonolito.game.dto.GameResponse;
import com.app.proyectojuegosmonolito.game.mapper.GameMapper;
import com.app.proyectojuegosmonolito.game.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Staff games", description = "Game management APIs for ADMIN and VENDEDOR")
@RestController
@RequestMapping("/api/v1/manage/games")
@RequiredArgsConstructor
public class ManageGameController {

    private final GameService gameService;
    private final GameMapper gameMapper;
    private final UserService userService;
    private final SecurityContext securityContext;

    @Operation(summary = "Staff game list", description = "ADMIN sees all games; VENDEDOR sees only their own catalog, optionally filtered by name")
    @ApiResponse(responseCode = "200", description = "List of games retrieved successfully")
    @GetMapping
    public ResponseEntity<Page<GameResponse>> findAll(
            @RequestParam(required = false) String name,
            @ParameterObject Pageable pageable) {
        var user = userService.findById(securityContext.getCurrentUserId());
        if (user.getRole() == Role.ADMIN) {
            if (name != null && !name.isBlank()) {
                return ResponseEntity.ok(gameService.findByName(name, pageable).map(gameMapper::toResponse));
            }
            return ResponseEntity.ok(gameService.findAll(pageable).map(gameMapper::toResponse));
        }
        return ResponseEntity.ok(gameService.findBySeller(user.getId(), name, pageable).map(gameMapper::toResponse));
    }

    @Operation(summary = "Staff game detail", description = "ADMIN can access any game; VENDEDOR only games assigned to them")
    @ApiResponse(responseCode = "200", description = "Game found")
    @ApiResponse(responseCode = "404", description = "Game not found or not owned")
    @GetMapping("/{id}")
    public ResponseEntity<GameResponse> findById(@PathVariable Long id) {
        var user = userService.findById(securityContext.getCurrentUserId());
        boolean allowed = user.getRole() == Role.ADMIN
                || gameService.isSellerOfGame(id, user.getId());
        if (!allowed) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(gameMapper.toResponse(gameService.findById(id)));
    }
}