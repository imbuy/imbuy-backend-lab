package imbuy.backend.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;


public record CategoryDto(
        Long id,
        @NotBlank(message = "Category name is required")
        String name,
        Long parentId,
        String parentName,
        List<CategoryDto> children
) {}
