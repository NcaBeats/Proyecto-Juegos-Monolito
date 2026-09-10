package com.app.proyectojuegosmonolito.blog.repository;

import com.app.proyectojuegosmonolito.blog.model.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogRepository extends JpaRepository<Blog, Long> {
    Page<Blog> findByOrderByPublishedAtDesc(Pageable pageable);
}
