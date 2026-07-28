package com.app.proyectojuegosmonolito.user.service;

import com.app.proyectojuegosmonolito.user.model.Visibility;
import com.app.proyectojuegosmonolito.user.repository.ProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.app.proyectojuegosmonolito.user.UserFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private ProfileService profileService;

    @Test
    void findByUserId_whenFound_shouldReturnProfile() {
        var user = user(1L);
        var profile = profile(user);
        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile));

        var result = profileService.findByUserId(1L);

        assertThat(result).isEqualTo(profile);
    }

    @Test
    void findByUserId_whenNotFound_shouldThrow() {
        when(profileRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.findByUserId(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void update_shouldModifyAndSave() {
        var user = user(1L);
        var profile = profile(user, "old", Visibility.PUBLIC);
        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = profileService.update(1L, "newnick", "avatar.png", "new bio", Visibility.PRIVATE);

        assertThat(result.getNickname()).isEqualTo("newnick");
        assertThat(result.getAvatarImage()).isEqualTo("avatar.png");
        assertThat(result.getBio()).isEqualTo("new bio");
        assertThat(result.getVisibility()).isEqualTo(Visibility.PRIVATE);
        verify(profileRepository).save(profile);
    }
}
