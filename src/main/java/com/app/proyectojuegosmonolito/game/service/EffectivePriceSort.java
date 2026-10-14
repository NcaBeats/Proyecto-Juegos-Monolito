package com.app.proyectojuegosmonolito.game.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Resuelve el sort virtual {@code price} (precio efectivo post-descuento).
 * {@code price} es {@code @Transient} en {@code Game} y el motor no puede
 * ordenar por el: el servicio lo detecta aqui y rutea a las queries con
 * ORDER BY sobre la expresion, en vez de dejar que el sort llegue al
 * repositorio y falle con {@code PropertyReferenceException}.
 */
final class EffectivePriceSort {

    static final String PRICE_PROPERTY = "price";

    private EffectivePriceSort() {
    }

    static boolean hasPriceSort(Pageable pageable) {
        return pageable.getSort().getOrderFor(PRICE_PROPERTY) != null;
    }

    static Sort.Direction priceDirection(Pageable pageable) {
        var order = pageable.getSort().getOrderFor(PRICE_PROPERTY);
        return order == null ? Sort.Direction.ASC : order.getDirection();
    }

    /**
     * Devuelve el {@code Pageable} sin sorts: las queries de precio efectivo
     * ya traen ORDER BY fijo y Spring agregaria un segundo ORDER BY si el
     * sort viajara. Conserva pagina y tamaño; los sorts distintos de
     * {@code price} se descartan (el llamador lo loguea).
     */
    static Pageable stripSort(Pageable pageable) {
        if (!pageable.isPaged()) {
            return Pageable.unpaged();
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
    }
}
