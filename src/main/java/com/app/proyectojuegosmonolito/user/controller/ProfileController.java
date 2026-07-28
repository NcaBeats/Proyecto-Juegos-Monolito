package com.app.proyectojuegosmonolito.user.controller;

import com.app.proyectojuegosmonolito.user.dto.ProfileRequest;
import com.app.proyectojuegosmonolito.user.dto.ProfileResponse;
import com.app.proyectojuegosmonolito.user.mapper.ProfileMapper;
import com.app.proyectojuegosmonolito.user.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Profile", description = "Profile management APIs")
@RestController
@RequestMapping("/api/v1/users/{userId}/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final ProfileMapper profileMapper;

    @Operation(summary = "Get profile by user", description = "Returns the profile for a specific user")
    @ApiResponse(responseCode = "200", description = "Profile found")
    @ApiResponse(responseCode = "404", description = "Profile not found")
    @GetMapping
    public ResponseEntity<ProfileResponse> findByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(profileMapper.toResponse(profileService.findByUserId(userId)));
    }

    @Operation(summary = "Update profile", description = "Updates the profile for a specific user")
    @ApiResponse(responseCode = "200", description = "Profile updated successfully")
    @ApiResponse(responseCode = "404", description = "Profile not found")
    @PutMapping
    public ResponseEntity<ProfileResponse> update(@PathVariable Long userId, @Valid @RequestBody ProfileRequest request) {
        var profile = profileService.update(userId, request.nickname(), request.avatarImage(), request.bio(), request.visibility());
        return ResponseEntity.ok(profileMapper.toResponse(profile));
    }
}
