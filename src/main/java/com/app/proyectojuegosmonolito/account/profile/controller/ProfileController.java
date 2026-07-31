package com.app.proyectojuegosmonolito.account.profile.controller;

import com.app.proyectojuegosmonolito.security.service.SecurityContext;
import com.app.proyectojuegosmonolito.account.profile.dto.ProfilePatchRequest;
import com.app.proyectojuegosmonolito.account.profile.dto.ProfileResponse;
import com.app.proyectojuegosmonolito.account.profile.mapper.ProfileMapper;
import com.app.proyectojuegosmonolito.account.profile.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Profile", description = "Profile management APIs")
@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final ProfileMapper profileMapper;
    private final SecurityContext securityContext;

    @Operation(summary = "Get profile", description = "Returns the profile for the authenticated user")
    @ApiResponse(responseCode = "200", description = "Profile found")
    @ApiResponse(responseCode = "404", description = "Profile not found")
    @GetMapping
    public ResponseEntity<ProfileResponse> findMyProfile() {
        var userId = securityContext.getCurrentUserId();
        return ResponseEntity.ok(profileMapper.toResponse(profileService.findByUserId(userId)));
    }

    @Operation(summary = "Get public profile by user ID", description = "Returns the profile for the specified user ID if it's public")
    @ApiResponse(responseCode = "200", description = "Profile found")
    @ApiResponse(responseCode = "404", description = "Profile not found or not public")
    @GetMapping("/{userId}")
    public ResponseEntity<ProfileResponse> findProfileByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(profileMapper.toResponse(profileService.findByUserIdPublic(userId)));
    }

    @Operation(summary = "Update profile", description = "Updates the profile for the authenticated user (partial update)")
    @ApiResponse(responseCode = "200", description = "Profile updated successfully")
    @ApiResponse(responseCode = "404", description = "Profile not found")
    @PatchMapping
    public ResponseEntity<ProfileResponse> updateMyProfile(@RequestBody ProfilePatchRequest request) {
        var userId = securityContext.getCurrentUserId();
        var profile = profileService.update(userId, request);
        return ResponseEntity.ok(profileMapper.toResponse(profile));
    }
}
