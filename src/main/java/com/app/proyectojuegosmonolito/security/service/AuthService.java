package com.app.proyectojuegosmonolito.security.service;

import com.app.proyectojuegosmonolito.security.dto.AuthResponse;
import com.app.proyectojuegosmonolito.security.dto.RegisterRequest;
import com.app.proyectojuegosmonolito.security.mapper.AuthMapper;
import com.app.proyectojuegosmonolito.account.user.model.User;
import com.app.proyectojuegosmonolito.account.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final AuthMapper authMapper;

    @Value("${app.jwt.expiration}")
    private long expiration;

    public AuthResponse login(String email, String password) {
        authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(email, password)
        );
        var user = userService.findByEmail(email);
        var token = jwtService.generateAccessToken(user);
        return new AuthResponse(token, expiration);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        var user = authMapper.toEntity(request);
        var profile = authMapper.toProfile(request, user);
        var saved = userService.create(user, profile);
        var token = jwtService.generateAccessToken(saved);
        return new AuthResponse(token, expiration);
    }

    @Transactional
    public void logout(Long userId) {
        userService.revokeAllTokens(userId);
    }
}
