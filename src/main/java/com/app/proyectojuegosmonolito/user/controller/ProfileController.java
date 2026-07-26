package com.app.proyectojuegosmonolito.user.controller;

import com.app.proyectojuegosmonolito.user.dto.ProfileRequest;
import com.app.proyectojuegosmonolito.user.dto.ProfileResponse;
import com.app.proyectojuegosmonolito.user.mapper.ProfileMapper;
import com.app.proyectojuegosmonolito.user.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/{userId}/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final ProfileMapper profileMapper;

    @GetMapping
    public ResponseEntity<ProfileResponse> findByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(profileMapper.toResponse(profileService.findByUserId(userId)));
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> update(@PathVariable Long userId, @Valid @RequestBody ProfileRequest request) {
        var profile = profileService.update(userId, request.nickname(), request.avatarImage(), request.bio(), request.visibility());
        return ResponseEntity.ok(profileMapper.toResponse(profile));
    }
}
