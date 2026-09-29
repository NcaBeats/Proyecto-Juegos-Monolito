package com.app.proyectojuegosmonolito.blog.mapper;

import com.app.proyectojuegosmonolito.blog.model.Blog;
import com.app.proyectojuegosmonolito.blog.dto.BlogRequest;
import com.app.proyectojuegosmonolito.blog.dto.BlogResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class BlogMapper {

    public Blog toEntity(BlogRequest request) {
        var now = Instant.now();
        return Blog.builder()
                .title(request.title())
                .excerpt(request.excerpt())
                .content(request.content())
                .coverImage(request.coverImage())
                .category(request.category())
                .publishedAt(now)
                .createdAt(now)
                .build();
    }

    public BlogResponse toResponse(Blog blog) {
        return new BlogResponse(
                blog.getId(),
                blog.getTitle(),
                blog.getExcerpt(),
                blog.getContent(),
                blog.getCoverImage(),
                blog.getCategory(),
                blog.getPublishedAt(),
                blog.getCreatedAt()
        );
    }
}
