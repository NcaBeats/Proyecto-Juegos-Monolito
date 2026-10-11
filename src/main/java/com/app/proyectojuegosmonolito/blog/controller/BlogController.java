package com.app.proyectojuegosmonolito.blog.controller;

import com.app.proyectojuegosmonolito.blog.dto.BlogCoverPresignRequest;
import com.app.proyectojuegosmonolito.blog.dto.BlogRequest;
import com.app.proyectojuegosmonolito.blog.dto.BlogResponse;
import com.app.proyectojuegosmonolito.blog.mapper.BlogMapper;
import com.app.proyectojuegosmonolito.blog.service.BlogService;
import com.app.proyectojuegosmonolito.common.storage.PresignedUploadResponse;
import com.app.proyectojuegosmonolito.common.storage.R2StorageService;
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
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Blogs", description = "Blog management APIs")
@RestController
@RequestMapping("/api/v1/blogs")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;
    private final BlogMapper blogMapper;
    private final R2StorageService r2StorageService;

    @Operation(summary = "Get all blogs", description = "Returns a paginated list of all blogs ordered by published date, optionally filtered by a search query matching title, excerpt or content")
    @ApiResponse(responseCode = "200", description = "List of blogs retrieved successfully")
    @GetMapping
    public ResponseEntity<Page<BlogResponse>> findAll(
            @RequestParam(required = false) String q,
            @ParameterObject Pageable pageable) {
        if (q != null && !q.isBlank()) {
            return ResponseEntity.ok(blogService.search(q, pageable).map(blogMapper::toResponse));
        }
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

    @Operation(summary = "Update a blog", description = "Updates an existing blog by its ID (ADMIN only)")
    @ApiResponse(responseCode = "200", description = "Blog updated successfully")
    @ApiResponse(responseCode = "404", description = "Blog not found")
    @PutMapping("/{id}")
    public ResponseEntity<BlogResponse> update(@PathVariable Long id, @Valid @RequestBody BlogRequest request) {
        var blog = blogService.update(id, request.title(), request.excerpt(), request.content(), request.coverImage(), request.category());
        return ResponseEntity.ok(blogMapper.toResponse(blog));
    }

    @Operation(summary = "Presign cover upload", description = "Returns a short-lived R2 URL so the browser can upload the blog cover " +
            "directly, bypassing the body size limit of serverless platforms. The returned contentType must be echoed " +
            "verbatim in the PUT header, otherwise R2 rejects the upload with SignatureDoesNotMatch. (ADMIN only)")
    @ApiResponse(responseCode = "200", description = "Presigned upload granted")
    @PostMapping("/media/cover/presign")
    public ResponseEntity<PresignedUploadResponse> presignCover(@Valid @RequestBody BlogCoverPresignRequest request) {
        var slug = blogService.coverSlug(request.title());
        return ResponseEntity.ok(r2StorageService.presignImage(slug, "cover", request.contentType()));
    }

    @Operation(summary = "Upload blog cover image", description = "Uploads a cover image for the specified blog (ADMIN only)")
    @ApiResponse(responseCode = "200", description = "Cover uploaded successfully")
    @ApiResponse(responseCode = "404", description = "Blog not found")
    @PostMapping("/{id}/cover")
    public ResponseEntity<BlogResponse> uploadCover(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws java.io.IOException {
        return ResponseEntity.ok(blogMapper.toResponse(blogService.uploadCover(id, file)));
    }
}
