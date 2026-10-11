package com.app.proyectojuegosmonolito.account.profile.repository;

import com.app.proyectojuegosmonolito.account.profile.model.Region;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {
    Optional<Region> findByNombre(String nombre);
}