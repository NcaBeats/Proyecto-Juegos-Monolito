package com.app.proyectojuegosmonolito.account.profile.service;

import com.app.proyectojuegosmonolito.account.profile.dto.ProfilePatchRequest;
import com.app.proyectojuegosmonolito.account.profile.model.Profile;
import com.app.proyectojuegosmonolito.account.profile.model.Visibility;
import com.app.proyectojuegosmonolito.account.profile.repository.ProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;

    public Profile findByUserId(Long userId) {
        log.info("Fetching profile for user: {}", userId);
        return profileRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Profile not found for user: {}", userId);
                    return new EntityNotFoundException("Profile not found: " + userId);
                });
    }

    public Profile findByUserIdPublic(Long userId) {
        var profile = findByUserId(userId);
        if (profile.getVisibility() != Visibility.PUBLIC) {
            log.warn("Profile for user {} is private", userId);
            throw new EntityNotFoundException("Profile not found: " + userId);
        }
        return profile;
    }

    @Transactional
    public Profile update(Long userId, ProfilePatchRequest request) {
        log.info("Updating profile for user {}", userId);
        var profile = findByUserId(userId);
        if (request.nickname() != null) profile.setNickname(request.nickname());
        if (request.bio() != null) profile.setBio(request.bio());
        if (request.visibility() != null) profile.setVisibility(request.visibility());
        if (request.firstName() != null) profile.setFirstName(request.firstName());
        if (request.lastName() != null) profile.setLastName(request.lastName());
        if (request.birthDate() != null) profile.setBirthDate(request.birthDate());
        if (request.region() != null) profile.setRegion(request.region());
        if (request.comuna() != null) profile.setComuna(request.comuna());
        if (request.address() != null) profile.setAddress(request.address());
        var saved = profileRepository.save(profile);
        log.info("Updated profile for user {}", userId);
        return saved;
    }
}
