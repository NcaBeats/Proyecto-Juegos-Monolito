package com.app.proyectojuegosmonolito.game.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.*;

class EffectivePriceSortTest {

    @Test
    void hasPriceSort_shouldDetectPriceProperty() {
        assertThat(EffectivePriceSort.hasPriceSort(PageRequest.of(0, 10, Sort.by("price").ascending()))).isTrue();
        assertThat(EffectivePriceSort.hasPriceSort(PageRequest.of(0, 10, Sort.by("name").ascending()))).isFalse();
        assertThat(EffectivePriceSort.hasPriceSort(PageRequest.of(0, 10))).isFalse();
    }

    @Test
    void priceDirection_shouldHonorSortDirection() {
        assertThat(EffectivePriceSort.priceDirection(PageRequest.of(0, 10, Sort.by("price").ascending())))
                .isEqualTo(Sort.Direction.ASC);
        assertThat(EffectivePriceSort.priceDirection(PageRequest.of(0, 10, Sort.by("price").descending())))
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void stripSort_shouldPreservePageAndDropSort() {
        var stripped = EffectivePriceSort.stripSort(PageRequest.of(2, 25, Sort.by("price").descending()));

        assertThat(stripped.getPageNumber()).isEqualTo(2);
        assertThat(stripped.getPageSize()).isEqualTo(25);
        assertThat(stripped.getSort().isSorted()).isFalse();
    }

    @Test
    void stripSort_shouldKeepUnpaged() {
        assertThat(EffectivePriceSort.stripSort(Pageable.unpaged()).isPaged()).isFalse();
    }
}
