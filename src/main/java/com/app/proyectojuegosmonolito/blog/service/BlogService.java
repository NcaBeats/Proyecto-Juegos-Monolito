package com.app.proyectojuegosmonolito.blog.service;

import com.app.proyectojuegosmonolito.blog.model.Blog;
import com.app.proyectojuegosmonolito.blog.model.BlogCategory;
import com.app.proyectojuegosmonolito.common.RepositoryUtils;
import com.app.proyectojuegosmonolito.blog.repository.BlogRepository;
import com.app.proyectojuegosmonolito.common.storage.R2StorageService;
import com.app.proyectojuegosmonolito.game.model.MediaUrl;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogService {

    private final BlogRepository blogRepository;
    private final R2StorageService r2StorageService;

    @Transactional(readOnly = true)
    public Page<Blog> findAll(Pageable pageable) {
        log.info("Fetching all blogs with pageable: {}", pageable);
        return blogRepository.findByOrderByPublishedAtDesc(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Blog> search(String q, Pageable pageable) {
        log.info("Searching blogs by '{}' with pageable: {}", q, pageable);
        return blogRepository
                .findByTitleContainingIgnoreCaseOrExcerptContainingIgnoreCaseOrContentContainingIgnoreCaseOrderByPublishedAtDesc(
                        q, q, q, pageable);
    }

    @Transactional(readOnly = true)
    public Blog findById(Long id) {
        log.info("Fetching blog by id: {}", id);
        return RepositoryUtils.findOrThrow(blogRepository, id, "Blog");
    }

    @Transactional
    public Blog create(Blog blog) {
        MediaUrl.requireAbsolute(blog.getCoverImage(), "coverImage");
        var saved = blogRepository.save(blog);
        log.info("Created blog: {} (id={})", saved.getTitle(), saved.getId());
        return saved;
    }

    @Transactional
    public Blog update(Long id, String title, String excerpt, String content, String coverImage, BlogCategory category) {
        MediaUrl.requireAbsolute(coverImage, "coverImage");
        log.info("Updating blog {}: title={}", id, title);
        var blog = findById(id);
        var oldCover = blog.getCoverImage();
        var effectiveCover = (coverImage == null || coverImage.isBlank()) ? oldCover : coverImage;
        blog.update(title, excerpt, content, effectiveCover, category);
        if (coverImage != null && !coverImage.isBlank() && !coverImage.equals(oldCover)) {
            afterCommit(() -> deleteQuietly(oldCover));
        }
        log.info("Updated blog {}", blog.getId());
        return blog;
    }

    @Transactional
    public void delete(Long id) {
        if (!blogRepository.existsById(id)) {
            log.warn("Attempted to delete non-existent blog: {}", id);
            throw new EntityNotFoundException("Blog not found: " + id);
        }
        var coverImage = blogRepository.findById(id).orElseThrow().getCoverImage();
        blogRepository.deleteById(id);
        afterCommit(() -> deleteQuietly(coverImage));
        log.info("Deleted blog: {}", id);
    }

    @Transactional
    public Blog updateCover(Long id, String coverImageUrl) {
        MediaUrl.requireAbsolute(coverImageUrl, "coverImage");
        log.info("Updating cover for blog {}: {}", id, coverImageUrl);
        var blog = findById(id);
        var oldCover = blog.getCoverImage();
        blog.setCoverImage(coverImageUrl);
        if (!coverImageUrl.equals(oldCover)) {
            afterCommit(() -> deleteQuietly(oldCover));
        }
        log.info("Updated cover for blog {}", blog.getId());
        return blog;
    }

    @Transactional
    public Blog uploadCover(Long id, MultipartFile file) throws IOException {
        var blog = findById(id);
        var url = r2StorageService.storeImage(file, coverSlug(blog.getTitle()), "cover");
        log.info("Stored cover to R2: {}", url);
        return updateCover(id, url);
    }

    public String coverSlug(String title) {
        String slug = title == null ? "" : title.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (slug.isBlank()) {
            slug = "post";
        }
        return "blogs/" + slug;
    }

    private void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }

    private void deleteQuietly(String url) {
        if (url == null || url.isBlank()) {
            return;
        }
        try {
            r2StorageService.deleteImage(url);
        } catch (RuntimeException e) {
            log.warn("Could not delete blog cover {}: {}", url, e.getMessage());
        }
    }
}
