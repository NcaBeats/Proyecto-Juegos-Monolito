package com.app.proyectojuegosmonolito.game.controller;

import com.app.proyectojuegosmonolito.game.dto.CategoryRequest;
import com.app.proyectojuegosmonolito.game.dto.CategoryResponse;
import com.app.proyectojuegosmonolito.game.mapper.CategoryMapper;
import com.app.proyectojuegosmonolito.game.service.CategoryService;
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

@Tag(name = "Categories", description = "Category management APIs")
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @Operation(summary = "Get all categories", description = "Returns a paginated list of all categories")
    @ApiResponse(responseCode = "200", description = "List of categories retrieved successfully")
    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> findAll(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(categoryService.findAll(pageable).map(categoryMapper::toResponse));
    }

    @Operation(summary = "Get category by ID", description = "Returns a single category by its ID")
    @ApiResponse(responseCode = "200", description = "Category found")
    @ApiResponse(responseCode = "404", description = "Category not found")
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryMapper.toResponse(categoryService.findById(id)));
    }

    @Operation(summary = "Create a new category", description = "Creates a new category with the provided details")
    @ApiResponse(responseCode = "201", description = "Category created successfully")
    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        var category = categoryService.create(categoryMapper.toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryMapper.toResponse(category));
    }

    @Operation(summary = "Update a category", description = "Updates an existing category by its ID")
    @ApiResponse(responseCode = "200", description = "Category updated successfully")
    @ApiResponse(responseCode = "404", description = "Category not found")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        var category = categoryService.update(id, request.name());
        return ResponseEntity.ok(categoryMapper.toResponse(category));
    }

    @Operation(summary = "Delete a category", description = "Deletes a category by its ID")
    @ApiResponse(responseCode = "204", description = "Category deleted successfully")
    @ApiResponse(responseCode = "404", description = "Category not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
