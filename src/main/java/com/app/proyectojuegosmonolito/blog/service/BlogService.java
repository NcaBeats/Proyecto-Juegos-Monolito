package com.app.proyectojuegosmonolito.blog.service;

import com.app.proyectojuegosmonolito.blog.model.Blog;
import com.app.proyectojuegosmonolito.common.RepositoryUtils;
import com.app.proyectojuegosmonolito.blog.repository.BlogRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogService {

    private final BlogRepository blogRepository;

    @Transactional(readOnly = true)
    public Page<Blog> findAll(Pageable pageable) {
        log.info("Fetching all blogs with pageable: {}", pageable);
        return blogRepository.findByOrderByPublishedAtDesc(pageable);
    }

    @Transactional(readOnly = true)
    public Blog findById(Long id) {
        log.info("Fetching blog by id: {}", id);
        return RepositoryUtils.findOrThrow(blogRepository, id, "Blog");
    }

    @Transactional
    public Blog create(Blog blog) {
        var saved = blogRepository.save(blog);
        log.info("Created blog: {} (id={})", saved.getTitle(), saved.getId());
        return saved;
    }

    @Transactional
    public Blog update(Long id, String title, String excerpt, String content, String coverImage, String category) {
        log.info("Updating blog {}: title={}", id, title);
        var blog = findById(id);
        blog.update(title, excerpt, content, coverImage, category);
        log.info("Updated blog {}", blog.getId());
        return blog;
    }

    @Transactional
    public void delete(Long id) {
        if (!blogRepository.existsById(id)) {
            log.warn("Attempted to delete non-existent blog: {}", id);
            throw new EntityNotFoundException("Blog not found: " + id);
        }
        blogRepository.deleteById(id);
        log.info("Deleted blog: {}", id);
    }
}
