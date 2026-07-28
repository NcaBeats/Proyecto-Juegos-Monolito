package com.app.proyectojuegosmonolito.user.service;

import com.app.proyectojuegosmonolito.user.model.Profile;
import com.app.proyectojuegosmonolito.user.model.Visibility;
import com.app.proyectojuegosmonolito.user.repository.ProfileRepository;
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

    @Transactional
    public Profile update(Long userId, String nickname, String avatarImage, String bio, Visibility visibility) {
        log.info("Updating profile for user {}: nickname={}", userId, nickname);
        var profile = findByUserId(userId);
        profile.update(nickname, avatarImage, bio, visibility);
        var saved = profileRepository.save(profile);
        log.info("Updated profile for user {}", userId);
        return saved;
    }
}
