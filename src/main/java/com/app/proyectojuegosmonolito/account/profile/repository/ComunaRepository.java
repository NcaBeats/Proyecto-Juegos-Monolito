package com.app.proyectojuegosmonolito.account.profile.repository;

import com.app.proyectojuegosmonolito.account.profile.model.Comuna;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComunaRepository extends JpaRepository<Comuna, Long> {
    List<Comuna> findByRegion_Id(Long regionId);
    Optional<Comuna> findByNombreAndRegion_Id(String nombre, Long regionId);
}