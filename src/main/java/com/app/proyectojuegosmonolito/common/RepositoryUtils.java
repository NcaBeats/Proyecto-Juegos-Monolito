package com.app.proyectojuegosmonolito.common;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

public final class RepositoryUtils {

    private RepositoryUtils() {
    }

    public static <T, ID> T findOrThrow(JpaRepository<T, ID> repository, ID id, String entityName) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(entityName + " not found: " + id));
    }
}