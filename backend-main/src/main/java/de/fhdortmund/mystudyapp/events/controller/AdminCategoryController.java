package de.fhdortmund.mystudyapp.events.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.fhdortmund.mystudyapp.common.exception.ResourceNotFoundException;
import de.fhdortmund.mystudyapp.common.response.ApiResponse;
import de.fhdortmund.mystudyapp.events.model.Category;
import de.fhdortmund.mystudyapp.events.repository.CategoryRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryRepository.findAll(), "Categories retrieved"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Category>> createCategory(
            @Valid @RequestBody CategoryRequest request) {
        String name = request.getName().trim();
        if (categoryRepository.existsByName(name)) {
            throw new IllegalArgumentException("Category '" + name + "' already exists");
        }

        Category category = Category.builder()
                .name(name)
                .build();
        Category saved = categoryRepository.save(category);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(saved, "Category created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> updateCategory(
            @PathVariable Integer id,
            @Valid @RequestBody CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        String newName = request.getName().trim();
        if (!category.getName().equalsIgnoreCase(newName) && categoryRepository.existsByName(newName)) {
            throw new IllegalArgumentException("Category '" + newName + "' already exists");
        }

        category.setName(newName);
        Category saved = categoryRepository.save(category);
        return ResponseEntity.ok(ApiResponse.success(saved, "Category updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        categoryRepository.delete(category);
        return ResponseEntity.ok(ApiResponse.success(null, "Category deleted"));
    }

    @Data
    public static class CategoryRequest {
        @NotBlank(message = "Category name is required")
        @Size(min = 1, max = 50, message = "Name must be between 1 and 50 characters")
        private String name;
    }
}