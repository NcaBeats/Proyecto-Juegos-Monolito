package com.app.proyectojuegosmonolito.security.controller;

import com.app.proyectojuegosmonolito.SecurityContext;
import com.app.proyectojuegosmonolito.security.dto.AuthResponse;
import com.app.proyectojuegosmonolito.security.dto.LoginRequest;
import com.app.proyectojuegosmonolito.security.dto.RegisterRequest;
import com.app.proyectojuegosmonolito.security.service.AuthService;
import com.app.proyectojuegosmonolito.security.service.LoginAttemptLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SecurityContext securityContext;
    private final LoginAttemptLimiter loginAttemptLimiter;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                              HttpServletRequest httpRequest) {
        String ip = clientIp(httpRequest);
        long retryAfter = loginAttemptLimiter.retryAfterSeconds(request.email(), ip);
        if (retryAfter > 0) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header(HttpHeaders.RETRY_AFTER, Long.toString(retryAfter))
                    .build();
        }
        try {
            var response = authService.login(request.email(), request.password());
            loginAttemptLimiter.onSuccess(request.email(), ip);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            loginAttemptLimiter.onFailure(request.email(), ip);
            throw e;
        }
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // Detras de un proxy/load balancer llega una cadena de IPs; la primera es la del cliente.
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr() == null ? "" : request.getRemoteAddr();
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        authService.logout(securityContext.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
