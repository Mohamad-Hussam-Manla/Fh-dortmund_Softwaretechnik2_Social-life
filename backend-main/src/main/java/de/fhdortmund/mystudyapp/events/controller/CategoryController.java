package de.fhdortmund.mystudyapp.events.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.fhdortmund.mystudyapp.common.response.ApiResponse;
import de.fhdortmund.mystudyapp.events.dto.CategoryDto;
import de.fhdortmund.mystudyapp.events.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/public/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getAllCategories() {
        List<CategoryDto> dtos = categoryRepository.findAllByOrderBySortOrderAsc().stream()
                .map(cat -> CategoryDto.builder()
                        .id(cat.getId())
                        .name(cat.getName())
                        .icon(cat.getIcon())
                        .color(cat.getColor())
                        .sortOrder(cat.getSortOrder())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(dtos, "Categories retrieved"));
    }
}