package com.app.proyectojuegosmonolito.game.repository;

import com.app.proyectojuegosmonolito.game.model.Game;
import com.app.proyectojuegosmonolito.game.model.GameState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {
    Page<Game> findByDiscountPercentGreaterThan(Integer discountPercent, Pageable pageable);
    Page<Game> findByBannerUrlIsNotNull(Pageable pageable);
    Page<Game> findByCategories_Name(String categoryName, Pageable pageable);
    Page<Game> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Optional<Game> findByName(String name);
    Page<Game> findBySeller_Id(Long sellerId, Pageable pageable);
    Page<Game> findBySeller_IdAndNameContainingIgnoreCase(Long sellerId, String name, Pageable pageable);
    Page<Game> findBySeller_IdAndState(Long sellerId, GameState state, Pageable pageable);
    Page<Game> findBySeller_IdAndNameContainingIgnoreCaseAndState(Long sellerId, String name, GameState state, Pageable pageable);
    boolean existsByIdAndSeller_Id(Long id, Long sellerId);
    long countByState(GameState state);

    @Query("""
            SELECT COALESCE(SUM(g.originalPrice * (100 - g.discountPercent) / 100), 0)
            FROM Game g
            WHERE g.state = :state
            """)
    BigDecimal sumDiscountedPriceByState(@Param("state") GameState state);

    @Query("""
            SELECT g FROM Game g
            WHERE g.originalPrice IS NULL OR g.originalPrice = 0
            """)
    Page<Game> findFreeToPlay(Pageable pageable);

    Page<Game> findByState(GameState state, Pageable pageable);

    Page<Game> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Busqueda combinable del catalogo: nombre (contiene, insensible), categoria
     * exacta y rango sobre el precio vigente (post-descuento, lo que el usuario
     * paga), no sobre el original.
     *
     * <p>El patron LIKE viaja ya armado desde el servicio ("%%" = sin filtro) y
     * nunca es null: un parametro String null lo bindea el driver como bytea y
     * Postgres rechaza `text LIKE bytea` al planificar, aunque la rama este
     * protegida por `:param IS NULL`. Por el mismo motivo la categoria lleva
     * CAST explicito a varchar.
     *
     * <p>El DISTINCT es obligatorio: el JOIN con categorias duplicaria filas en
     * juegos con varias categorias y romperia el contenido y el total.
     */
    @Query(value = """
            SELECT DISTINCT g FROM Game g LEFT JOIN g.categories c
            WHERE LOWER(g.name) LIKE :pattern
              AND (:category IS NULL OR c.name = CAST(:category AS String))
              AND (:state IS NULL OR g.state = CAST(:state AS String))
              AND (:min IS NULL OR g.originalPrice * (100 - g.discountPercent) / 100 >= :min)
              AND (:max IS NULL OR g.originalPrice * (100 - g.discountPercent) / 100 <= :max)
            """,
            countQuery = """
            SELECT COUNT(DISTINCT g) FROM Game g LEFT JOIN g.categories c
            WHERE LOWER(g.name) LIKE :pattern
              AND (:category IS NULL OR c.name = CAST(:category AS String))
              AND (:state IS NULL OR g.state = CAST(:state AS String))
              AND (:min IS NULL OR g.originalPrice * (100 - g.discountPercent) / 100 >= :min)
              AND (:max IS NULL OR g.originalPrice * (100 - g.discountPercent) / 100 <= :max)
            """)
    Page<Game> search(@Param("pattern") String pattern,
                      @Param("category") String category,
                      @Param("state") String state,
                      @Param("min") BigDecimal min,
                      @Param("max") BigDecimal max,
                      Pageable pageable);

    /**
     * Variante de {@link #search} con orden fijo por precio efectivo
     * (post-descuento) y desempate por id: el {@code Pageable} debe viajar
     * sin sort (el ORDER BY ya va en la query) y la propiedad virtual
     * {@code price} (@Transient) se resuelve en el servicio. El
     * {@code sellerId} nulable permite reusarla en catalogo, ADMIN y
     * VENDEDOR sin duplicar queries.
     */
    @Query(value = """
            SELECT DISTINCT g FROM Game g LEFT JOIN g.categories c
            WHERE (:sellerId IS NULL OR g.seller.id = :sellerId)
              AND LOWER(g.name) LIKE :pattern
              AND (:category IS NULL OR c.name = CAST(:category AS String))
              AND (:state IS NULL OR g.state = CAST(:state AS String))
              AND (:min IS NULL OR g.originalPrice * (100 - g.discountPercent) / 100 >= :min)
              AND (:max IS NULL OR g.originalPrice * (100 - g.discountPercent) / 100 <= :max)
            ORDER BY (g.originalPrice * (100 - g.discountPercent) / 100) ASC, g.id ASC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT g) FROM Game g LEFT JOIN g.categories c
            WHERE (:sellerId IS NULL OR g.seller.id = :sellerId)
              AND LOWER(g.name) LIKE :pattern
              AND (:category IS NULL OR c.name = CAST(:category AS String))
              AND (:state IS NULL OR g.state = CAST(:state AS String))
              AND (:min IS NULL OR g.originalPrice * (100 - g.discountPercent) / 100 >= :min)
              AND (:max IS NULL OR g.originalPrice * (100 - g.discountPercent) / 100 <= :max)
            """)
    Page<Game> searchOrderByEffectivePriceAsc(@Param("pattern") String pattern,
                                              @Param("category") String category,
                                              @Param("state") String state,
                                              @Param("min") BigDecimal min,
                                              @Param("max") BigDecimal max,
                                              @Param("sellerId") Long sellerId,
                                              Pageable pageable);

    @Query(value = """
            SELECT DISTINCT g FROM Game g LEFT JOIN g.categories c
            WHERE (:sellerId IS NULL OR g.seller.id = :sellerId)
              AND LOWER(g.name) LIKE :pattern
              AND (:category IS NULL OR c.name = CAST(:category AS String))
              AND (:state IS NULL OR g.state = CAST(:state AS String))
              AND (:min IS NULL OR g.originalPrice * (100 - g.discountPercent) / 100 >= :min)
              AND (:max IS NULL OR g.originalPrice * (100 - g.discountPercent) / 100 <= :max)
            ORDER BY (g.originalPrice * (100 - g.discountPercent) / 100) DESC, g.id ASC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT g) FROM Game g LEFT JOIN g.categories c
            WHERE (:sellerId IS NULL OR g.seller.id = :sellerId)
              AND LOWER(g.name) LIKE :pattern
              AND (:category IS NULL OR c.name = CAST(:category AS String))
              AND (:state IS NULL OR g.state = CAST(:state AS String))
              AND (:min IS NULL OR g.originalPrice * (100 - g.discountPercent) / 100 >= :min)
              AND (:max IS NULL OR g.originalPrice * (100 - g.discountPercent) / 100 <= :max)
            """)
    Page<Game> searchOrderByEffectivePriceDesc(@Param("pattern") String pattern,
                                               @Param("category") String category,
                                               @Param("state") String state,
                                               @Param("min") BigDecimal min,
                                               @Param("max") BigDecimal max,
                                               @Param("sellerId") Long sellerId,
                                               Pageable pageable);
}
