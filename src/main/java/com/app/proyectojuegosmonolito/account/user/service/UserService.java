package com.app.proyectojuegosmonolito.account.user.service;

import com.app.proyectojuegosmonolito.TokenVersionCache;
import com.app.proyectojuegosmonolito.account.profile.model.Profile;
import com.app.proyectojuegosmonolito.account.user.model.Role;
import com.app.proyectojuegosmonolito.account.user.model.User;
import com.app.proyectojuegosmonolito.account.wallet.model.Wallet;
import com.app.proyectojuegosmonolito.account.profile.model.Visibility;
import com.app.proyectojuegosmonolito.account.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TokenVersionCache tokenVersionCache;

    @Transactional
    public User create(User user) {
        return create(user, null);
    }

    @Transactional
    public User create(User user, Profile profile) {
        var now = Instant.now();
        user.setCreatedAt(now);

        if (profile != null) {
            profile.setUser(user);
            if (profile.getCreatedAt() == null) {
                profile.setCreatedAt(now);
            }
            user.setProfile(profile);
        } else {
            user.setProfile(Profile.builder()
                    .user(user)
                    .nickname(user.getUsername())
                    .visibility(Visibility.PUBLIC)
                    .createdAt(now)
                    .build());
        }

        user.setWallet(Wallet.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .updatedAt(now)
                .build());
        if (user.getRole() == null) {
            user.setRole(Role.CLIENTE);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        var saved = userRepository.save(user);
        log.info("Created user: {} (id={})", saved.getUsername(), saved.getId());
        return saved;
    }

    public User findById(Long id) {
        log.info("Fetching user by id: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", id);
                    return new EntityNotFoundException("User not found: " + id);
                });
    }

    public User findByEmail (String email) {
        log.info("Fetching user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new EntityNotFoundException("User not found with email: " + email);
                });
    }

    public Page<User> findAll(Pageable pageable) {
        log.info("Fetching all users with pageable: {}", pageable);
        return userRepository.findAll(pageable);
    }

    @Transactional
    public User update(Long id, String username, String email) {
        var userEntity = userRepository.findById(id).orElseThrow(() -> {
            log.warn("User not found: {}", id);
            return new EntityNotFoundException("User not found: " + id);
        });
        log.info("Updating user {}: username={}", id, username.replaceAll("[\\r\\n]", " "));
        userEntity.update(username, email);
        log.info("Updated user {}", userEntity.getId());
        return userEntity;
    }

    @Transactional
    public void updatePassword (Long id, String currentPassword, String newPassword) {
        var user = userRepository.findById(id).orElseThrow(() -> {
            log.warn("User not found with id: {}", id);
            return new EntityNotFoundException("User not found with id: " + id);
        });
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        incrementTokenVersion(user);
    }

    @Transactional
    public int revokeAllTokens(Long id) {
        var user = userRepository.findById(id).orElseThrow(() -> {
            log.warn("User not found with id: {}", id);
            return new EntityNotFoundException("User not found with id: " + id);
        });
        return incrementTokenVersion(user);
    }

    public Optional<Integer> getTokenVersion(Long id) {
        return userRepository.findById(id).map(User::getTokenVersion);
    }

    private int incrementTokenVersion(User user) {
        var newVersion = user.getTokenVersion() + 1;
        user.setTokenVersion(newVersion);
        log.info("Invalidated tokens for user {}: tokenVersion={}", user.getId(), newVersion);
        tokenVersionCache.set(user.getId(), newVersion);
        return newVersion;
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            log.warn("Attempted to delete non-existent user: {}", id);
            throw new EntityNotFoundException("User not found: " + id);
        }
        userRepository.deleteById(id);
        log.info("Deleted user {}", id);
    }
}
