package com.app.proyectojuegosmonolito.account.profile.repository;

import com.app.proyectojuegosmonolito.account.profile.model.Comuna;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComunaRepository extends JpaRepository<Comuna, Long> {
    @EntityGraph(attributePaths = {"region"})
    List<Comuna> findByRegion_Id(Long regionId);

    @EntityGraph(attributePaths = {"region"})
    Optional<Comuna> findByNombreAndRegion_Id(String nombre, Long regionId);
}