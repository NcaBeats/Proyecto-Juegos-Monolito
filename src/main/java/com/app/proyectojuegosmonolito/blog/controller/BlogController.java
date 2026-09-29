package com.app.proyectojuegosmonolito.blog.controller;

import com.app.proyectojuegosmonolito.blog.dto.BlogRequest;
import com.app.proyectojuegosmonolito.blog.dto.BlogResponse;
import com.app.proyectojuegosmonolito.blog.mapper.BlogMapper;
import com.app.proyectojuegosmonolito.blog.service.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Blogs", description = "Blog management APIs")
@RestController
@RequestMapping("/api/v1/blogs")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;
    private final BlogMapper blogMapper;

    @Operation(summary = "Get all blogs", description = "Returns a paginated list of all blogs ordered by published date")
    @ApiResponse(responseCode = "200", description = "List of blogs retrieved successfully")
    @GetMapping
    public ResponseEntity<Page<BlogResponse>> findAll(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(blogService.findAll(pageable).map(blogMapper::toResponse));
    }

    @Operation(summary = "Get blog by ID", description = "Returns a single blog by its ID")
    @ApiResponse(responseCode = "200", description = "Blog found")
    @ApiResponse(responseCode = "404", description = "Blog not found")
    @GetMapping("/{id}")
    public ResponseEntity<BlogResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(blogMapper.toResponse(blogService.findById(id)));
    }

    @Operation(summary = "Create a new blog", description = "Creates a new blog (ADMIN only)")
    @ApiResponse(responseCode = "201", description = "Blog created successfully")
    @PostMapping
    public ResponseEntity<BlogResponse> create(@Valid @RequestBody BlogRequest request) {
        var blog = blogMapper.toEntity(request);
        var saved = blogService.create(blog);
        return ResponseEntity.status(HttpStatus.CREATED).body(blogMapper.toResponse(saved));
    }

    @Operation(summary = "Delete a blog by ID", description = "Deletes a blog (ADMIN only)")
    @ApiResponse(responseCode = "204", description = "Blog deleted successfully")
    @ApiResponse(responseCode = "404", description = "Blog not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        blogService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
