package com.app.proyectojuegosmonolito.user.service;

import com.app.proyectojuegosmonolito.user.Profile;
import com.app.proyectojuegosmonolito.user.User;
import com.app.proyectojuegosmonolito.user.Wallet;
import com.app.proyectojuegosmonolito.user.model.Visibility;
import com.app.proyectojuegosmonolito.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
        return userRepository.save(user);
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
    }

    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional
    public User update(Long id, String username, String email, String password) {
        var user = findById(id);
        user.update(username, email, password);
        return userRepository.save(user);
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found: " + id);
        }
        userRepository.deleteById(id);
    }
}
