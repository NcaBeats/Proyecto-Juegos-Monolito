package com.app.proyectojuegosmonolito.user.mapper;

import com.app.proyectojuegosmonolito.user.model.Profile;
import com.app.proyectojuegosmonolito.user.dto.ProfileResponse;
import org.springframework.stereotype.Component;

@Component
public class ProfileMapper {

    public ProfileResponse toResponse(Profile profile) {
        return new ProfileResponse(
                profile.getUserId(),
                profile.getNickname(),
                profile.getAvatarImage(),
                profile.getBio(),
                profile.getVisibility(),
                profile.getCreatedAt()
        );
    }
}
