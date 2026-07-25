package com.app.proyectojuegosmonolito.library.repository;

import com.app.proyectojuegosmonolito.library.Library;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LibraryRepository extends JpaRepository<Library, Long> {
    Page<Library> findByUser_Id(Long userId, Pageable pageable);
}
