package com.app.proyectojuegosmonolito.common;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Traduce un {@link Optional} vacio a {@link EntityNotFoundException}, que
 * {@code GlobalExceptionHandler} traduce a 404.
 *
 * Existia porque tres servicios lo usaban y los demas (UserService seis veces,
 * CategoryService, BlogService, ProfileService, LibraryService) reescribian el
 * mismo bloque a mano. Como las copias tenian su propio {@code log.warn}, la
 * regla se habia ido desincronizando: unos logueaban y otros no. El log vive
 * aca adentro, una sola vez.
 */
public final class RepositoryUtils {

    private static final Logger log = LoggerFactory.getLogger(RepositoryUtils.class);

    private RepositoryUtils() {
    }

    public static <T, ID> T findOrThrow(JpaRepository<T, ID> repository, ID id, String entityName) {
        return orNotFound(repository.findById(id), entityName, id);
    }

    /** Para lookups por un campo que no es la clave primaria. */
    public static <T> T orNotFound(Optional<T> found, String entityName, Object key) {
        return found.orElseThrow(() -> {
            log.warn("{} not found: {}", entityName, key);
            return new EntityNotFoundException(entityName + " not found: " + key);
        });
    }
}
