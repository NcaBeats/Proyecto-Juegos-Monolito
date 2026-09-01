package com.app.proyectojuegosmonolito.account.profile.repository;

import com.app.proyectojuegosmonolito.account.profile.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
}
