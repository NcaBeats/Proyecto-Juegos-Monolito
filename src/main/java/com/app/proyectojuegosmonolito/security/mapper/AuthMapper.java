package com.app.proyectojuegosmonolito.security.mapper;

import com.app.proyectojuegosmonolito.security.dto.RegisterRequest;
import com.app.proyectojuegosmonolito.account.user.model.User;
import com.app.proyectojuegosmonolito.account.profile.model.Profile;
import com.app.proyectojuegosmonolito.account.profile.model.Region;
import com.app.proyectojuegosmonolito.account.profile.model.Visibility;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AuthMapper {

    public User toEntity(RegisterRequest request) {
        return User.builder()
                .email(request.email())
                .password(request.password())
                .build();
    }

    public Profile toProfile(RegisterRequest request, User user) {
        return Profile.builder()
                .user(user)
                .nickname(request.email())
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
}
