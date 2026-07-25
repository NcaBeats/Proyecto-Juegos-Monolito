package com.app.proyectojuegosmonolito.user.mapper;

import com.app.proyectojuegosmonolito.user.User;
import com.app.proyectojuegosmonolito.user.dto.UserRequest;
import com.app.proyectojuegosmonolito.user.dto.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(UserRequest request) {
        return User.builder()
                .username(request.username())
                .email(request.email())
                .password(request.password())
                .build();
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}
