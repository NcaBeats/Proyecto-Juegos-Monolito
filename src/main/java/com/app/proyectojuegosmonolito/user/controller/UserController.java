package com.app.proyectojuegosmonolito.user.controller;

import com.app.proyectojuegosmonolito.user.dto.UserRequest;
import com.app.proyectojuegosmonolito.user.dto.UserResponse;
import com.app.proyectojuegosmonolito.user.mapper.UserMapper;
import com.app.proyectojuegosmonolito.user.service.UserService;
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

@Tag(name = "Users", description = "User management APIs")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @Operation(summary = "Get all users", description = "Returns a paginated list of all users")
    @ApiResponse(responseCode = "200", description = "List of users retrieved successfully")
    @GetMapping
    public ResponseEntity<Page<UserResponse>> findAll(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(userService.findAll(pageable).map(userMapper::toResponse));
    }

    @Operation(summary = "Get user by ID", description = "Returns a single user by its ID")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(userMapper.toResponse(userService.findById(id)));
    }

    @Operation(summary = "Create a new user", description = "Creates a new user with auto-generated profile and wallet")
    @ApiResponse(responseCode = "201", description = "User created successfully")
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        var user = userService.create(userMapper.toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(user));
    }

    @Operation(summary = "Update a user", description = "Updates an existing user by its ID")
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        var user = userService.update(id, request.username(), request.email(), request.password());
        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    @Operation(summary = "Delete a user", description = "Deletes a user by its ID")
    @ApiResponse(responseCode = "204", description = "User deleted successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
