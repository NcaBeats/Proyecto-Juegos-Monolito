package com.app.proyectojuegosmonolito.account.profile.mapper;

import com.app.proyectojuegosmonolito.account.profile.model.Profile;
import com.app.proyectojuegosmonolito.account.profile.dto.ProfileResponse;
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
                profile.getRun(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getBirthDate(),
                profile.getRegion(),
                profile.getComuna(),
                profile.getAddress(),
                profile.getCreatedAt()
        );
    }
}
