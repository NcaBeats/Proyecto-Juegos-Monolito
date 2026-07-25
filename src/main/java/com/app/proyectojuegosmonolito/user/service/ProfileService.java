package com.app.proyectojuegosmonolito.user.service;

import com.app.proyectojuegosmonolito.user.Profile;
import com.app.proyectojuegosmonolito.user.model.Visibility;
import com.app.proyectojuegosmonolito.user.repository.ProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;

    public Profile findByUserId(Long userId) {
        return profileRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found: " + userId));
    }

    @Transactional
    public Profile update(Long userId, String nickname, String avatarImage, String bio, Visibility visibility) {
        var profile = findByUserId(userId);
        profile.update(nickname, avatarImage, bio, visibility);
        return profileRepository.save(profile);
    }
}
