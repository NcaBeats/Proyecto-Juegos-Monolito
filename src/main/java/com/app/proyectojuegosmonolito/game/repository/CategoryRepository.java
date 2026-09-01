package com.app.proyectojuegosmonolito.game.repository;

import com.app.proyectojuegosmonolito.game.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
