package com.app.proyectojuegosmonolito.user.service;

import com.app.proyectojuegosmonolito.TestcontainersConfiguration;
import com.app.proyectojuegosmonolito.user.model.Visibility;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static com.app.proyectojuegosmonolito.user.UserFixtures.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class ProfileServiceIT {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private UserService userService;

    @Test
    void findByUserId_shouldReturnProfile() {
        var user = userService.create(user());

        var profile = profileService.findByUserId(user.getId());

        assertThat(profile.getNickname()).isEqualTo("user");
        assertThat(profile.getVisibility()).isEqualTo(Visibility.PUBLIC);
    }

    @Test
    void findByUserId_whenNotFound_shouldThrow() {
        assertThatThrownBy(() -> profileService.findByUserId(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void update_shouldModifyAndSave() {
        var user = userService.create(user());

        var result = profileService.update(user.getId(), "newnick", "avatar.png", "new bio", Visibility.PRIVATE);

        assertThat(result.getNickname()).isEqualTo("newnick");
        assertThat(result.getAvatarImage()).isEqualTo("avatar.png");
        assertThat(result.getBio()).isEqualTo("new bio");
        assertThat(result.getVisibility()).isEqualTo(Visibility.PRIVATE);
    }
}
