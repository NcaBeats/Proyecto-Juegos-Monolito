package com.app.proyectojuegosmonolito.account.user.mapper;

import com.app.proyectojuegosmonolito.account.user.model.User;
import com.app.proyectojuegosmonolito.account.user.dto.UserRequestCreate;
import com.app.proyectojuegosmonolito.account.user.dto.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntityCreate(UserRequestCreate request) {
        return User.builder()
                .email(request.email())
                .password(request.password())
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
