package com.app.proyectojuegosmonolito.account.user.controller;

import com.app.proyectojuegosmonolito.SecurityContext;
import com.app.proyectojuegosmonolito.account.user.dto.UserRequestCreate;
import com.app.proyectojuegosmonolito.account.user.dto.UserResponse;
import com.app.proyectojuegosmonolito.account.user.dto.UserUpdatePassword;
import com.app.proyectojuegosmonolito.account.user.dto.UserUpdateRequest;
import com.app.proyectojuegosmonolito.account.user.mapper.UserMapper;
import com.app.proyectojuegosmonolito.account.user.service.UserService;
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
    private final SecurityContext securityContext;

    @Operation(summary = "Get all users", description = "Returns a paginated list of all users")
    @ApiResponse(responseCode = "200", description = "List of users retrieved successfully")
    @GetMapping
    public ResponseEntity<Page<UserResponse>> findAll(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(userService.findAll(pageable).map(userMapper::toResponse));
    }

    @Operation(summary = "Get current user", description = "Returns the authenticated user")
    @ApiResponse(responseCode = "200", description = "User found")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        var userId = securityContext.getCurrentUserId();
        return ResponseEntity.ok(userMapper.toResponse(userService.findById(userId)));
    }

    @Operation(summary = "Create a new user", description = "Creates a new user with auto-generated profile and wallet")
    @ApiResponse(responseCode = "201", description = "User created successfully")
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequestCreate request) {
        var user = userService.create(userMapper.toEntityCreate(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(user));
    }

    @Operation(summary = "Update my user", description = "Updates the authenticated user")
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PutMapping
    public ResponseEntity<UserResponse> update(@Valid @RequestBody UserUpdateRequest request) {
        var id = securityContext.getCurrentUserId();
        var user = userService.update(id, request.username(), request.email());
        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    @Operation(summary = "Update my password", description = "Updates the authenticated user's password")
    @ApiResponse(responseCode = "200", description = "Password updated successfully")
    @ApiResponse(responseCode = "400", description = "Current password is incorrect")
    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody UserUpdatePassword request) {
        var id = securityContext.getCurrentUserId();
        userService.updatePassword(id, request.currentPassword(), request.newPassword());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete my user", description = "Deletes the authenticated user")
    @ApiResponse(responseCode = "204", description = "User deleted successfully")
    @DeleteMapping
    public ResponseEntity<Void> delete() {
        var id = securityContext.getCurrentUserId();
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
