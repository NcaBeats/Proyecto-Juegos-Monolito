package com.app.proyectojuegosmonolito.account.user.service;

import com.app.proyectojuegosmonolito.TokenVersionCache;
import com.app.proyectojuegosmonolito.account.user.model.User;
import com.app.proyectojuegosmonolito.account.profile.model.Visibility;
import com.app.proyectojuegosmonolito.account.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static com.app.proyectojuegosmonolito.account.user.UserFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenVersionCache tokenVersionCache;

    @InjectMocks
    private UserService userService;

    @Test
    void create_shouldSetCreatedAtProfileAndWallet() {
        var user = user();
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            var u = i.<User>getArgument(0);
            u.setId(1L);
            return u;
        });
        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("encoded");

        var result = userService.create(user);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getProfile()).isNotNull();
        assertThat(result.getProfile().getNickname()).isEqualTo("user@test.com");
        assertThat(result.getProfile().getVisibility()).isEqualTo(Visibility.PUBLIC);
        assertThat(result.getWallet()).isNotNull();
        assertThat(result.getWallet().getBalance()).isEqualByComparingTo("0");
        verify(userRepository).save(user);
    }

    @Test
    void findById_whenFound_shouldReturnUser() {
        var user = user(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        var result = userService.findById(1L);

        assertThat(result).isEqualTo(user);
    }

    @Test
    void findById_whenNotFound_shouldThrow() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void findAll_shouldReturnPage() {
        var pageable = PageRequest.of(0, 10);
        var users = List.of(user(1L), user(2L));
        var page = new PageImpl<>(users, pageable, 2);
        when(userRepository.findAll(pageable)).thenReturn(page);

        var result = userService.findAll(pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void update_shouldModifyAndSave() {
        var user = user(1L, "old@test.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        var result = userService.update(1L, "new@test.com");

        assertThat(result.getEmail()).isEqualTo("new@test.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void delete_whenExists_shouldDelete() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.delete(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void delete_whenNotFound_shouldThrow() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.delete(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void revokeAllTokens_shouldIncrementVersionAndUpdateCache() {
        var user = user(1L);
        user.setTokenVersion(2);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        var newVersion = userService.revokeAllTokens(1L);

        assertThat(newVersion).isEqualTo(3);
        assertThat(user.getTokenVersion()).isEqualTo(3);
        verify(tokenVersionCache).set(1L, 3);
    }

    @Test
    void revokeAllTokens_whenNotFound_shouldThrow() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.revokeAllTokens(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(tokenVersionCache, never()).set(any(), anyInt());
    }

    @Test
    void getTokenVersion_whenFound_shouldReturnVersion() {
        var user = user(1L);
        user.setTokenVersion(4);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThat(userService.getTokenVersion(1L)).hasValue(4);
    }

    @Test
    void getTokenVersion_whenNotFound_shouldReturnEmpty() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(userService.getTokenVersion(99L)).isEmpty();
    }

    @Test
    void updatePassword_shouldEncodeAndInvalidateTokens() {
        var user = user(1L);
        user.setTokenVersion(1);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current", "pass123")).thenReturn(true);
        when(passwordEncoder.encode("newpass")).thenReturn("encoded-new");

        userService.updatePassword(1L, "current", "newpass");

        assertThat(user.getPassword()).isEqualTo("encoded-new");
        assertThat(user.getTokenVersion()).isEqualTo(2);
        verify(tokenVersionCache).set(1L, 2);
    }
}
