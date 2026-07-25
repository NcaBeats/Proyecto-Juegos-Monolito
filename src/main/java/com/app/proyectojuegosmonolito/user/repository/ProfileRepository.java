package com.app.proyectojuegosmonolito.user.repository;

import com.app.proyectojuegosmonolito.user.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
}
