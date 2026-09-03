package com.app.proyectojuegosmonolito.game.service;

import com.app.proyectojuegosmonolito.game.model.Category;
import com.app.proyectojuegosmonolito.game.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public Category create(Category category) {
        category.setCreatedAt(Instant.now());
        var saved = categoryRepository.save(category);
        log.info("Created category: {} (id={})", saved.getName(), saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Category findById(Long id) {
        log.info("Fetching category by id: {}", id);
        return categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Category not found: {}", id);
                    return new EntityNotFoundException("Category not found: " + id);
                });
    }

    @Transactional(readOnly = true)
    public Page<Category> findAll(Pageable pageable) {
        log.info("Fetching all categories with pageable: {}", pageable);
        return categoryRepository.findAll(pageable);
    }

    @Transactional
    public Category update(Long id, String name) {
        log.info("Updating category {}: name={}", id, name);
        var category = findById(id);
        category.update(name);
        log.info("Updated category {}", category.getId());
        return category;
    }

    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            log.warn("Attempted to delete non-existent category: {}", id);
            throw new EntityNotFoundException("Category not found: " + id);
        }
        categoryRepository.deleteById(id);
        log.info("Deleted category {}", id);
    }
}
