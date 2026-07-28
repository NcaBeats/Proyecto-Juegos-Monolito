package com.app.proyectojuegosmonolito.user.service;

import com.app.proyectojuegosmonolito.user.model.Profile;
import com.app.proyectojuegosmonolito.user.model.User;
import com.app.proyectojuegosmonolito.user.model.Wallet;
import com.app.proyectojuegosmonolito.user.model.Visibility;
import com.app.proyectojuegosmonolito.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User create(User user) {
        var now = Instant.now();
        user.setCreatedAt(now);
        user.setProfile(Profile.builder()
                .user(user)
                .nickname(user.getUsername())
                .visibility(Visibility.PUBLIC)
                .createdAt(now)
                .build());
        user.setWallet(Wallet.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .updatedAt(now)
                .build());
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

    public Page<User> findAll(Pageable pageable) {
        log.info("Fetching all users with pageable: {}", pageable);
        return userRepository.findAll(pageable);
    }

    @Transactional
    public User update(Long id, String username, String email, String password) {
        log.info("Updating user {}: username={}", id, username);
        var user = findById(id);
        user.update(username, email, password);
        var saved = userRepository.save(user);
        log.info("Updated user {}", saved.getId());
        return saved;
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
