package com.app.proyectojuegosmonolito.account.profile.repository;

import com.app.proyectojuegosmonolito.account.profile.model.Profile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    @EntityGraph(attributePaths = {"region", "comuna"})
    @Query("SELECT p FROM Profile p WHERE p.userId = :id")
    Optional<Profile> findByIdWithRegionAndComuna(@org.springframework.data.repository.query.Param("id") Long id);
}
