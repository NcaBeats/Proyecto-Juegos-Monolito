package com.app.proyectojuegosmonolito.account.user.mapper;

import com.app.proyectojuegosmonolito.account.profile.model.Profile;
import com.app.proyectojuegosmonolito.account.profile.model.Visibility;
import com.app.proyectojuegosmonolito.account.user.model.User;
import com.app.proyectojuegosmonolito.account.user.dto.UserRequestCreate;
import com.app.proyectojuegosmonolito.account.user.dto.UserResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class UserMapper {

    public User toEntityCreate(UserRequestCreate request) {
        return User.builder()
                .email(request.email())
                .password(request.password())
                .build();
    }

    public Profile toProfileCreate(UserRequestCreate request, User user) {
        return Profile.builder()
                .user(user)
                .nickname(user.getEmail())
                .run(request.run())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .birthDate(request.birthDate())
                .region(request.region())
                .comuna(request.comuna())
                .address(request.address())
                .visibility(Visibility.PUBLIC)
                .createdAt(Instant.now())
                .build();
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}