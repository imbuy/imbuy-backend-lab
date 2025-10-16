package imbuy.backend.service;

import imbuy.backend.domain.Category;
import imbuy.backend.dto.CategoryDto;
import imbuy.backend.dto.CategoryTreeDto;
import imbuy.backend.dto.PageResponse;
import imbuy.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryTreeDto getCategoryTree() {
        List<Category> rootCategories = categoryRepository.findRootCategoriesWithChildren();
        CategoryTreeDto treeDto = new CategoryTreeDto();
        treeDto.setCategories(rootCategories.stream()
                .map(this::mapToDtoWithChildren)
                .collect(Collectors.toList()));
        return treeDto;
    }

    public PageResponse<CategoryDto> getAllCategories(Pageable pageable) {
        Page<Category> categories = categoryRepository.findAll(pageable);
        return PageResponse.of(categories.map(this::mapToDto));
    }

    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        return mapToDto(category);
    }

    public CategoryDto createCategory(CategoryDto categoryDto) {
        if (categoryRepository.existsByNameAndParentId(categoryDto.getName(), categoryDto.getParentId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category with this name already exists");
        }

        Category category = new Category();
        category.setName(categoryDto.getName());

        if (categoryDto.getParentId() != null) {
            Category parent = categoryRepository.findById(categoryDto.getParentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent category not found"));
            category.setParent(parent);
        }

        category = categoryRepository.save(category);
        return mapToDto(category);
    }

    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        category.setName(categoryDto.getName());

        if (categoryDto.getParentId() != null) {
            Category parent = categoryRepository.findById(categoryDto.getParentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent category not found"));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        category = categoryRepository.save(category);
        return mapToDto(category);
    }

    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        if (!category.getChildren().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete category with subcategories");
        }

        if (!category.getLots().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete category with associated lots");
        }

        categoryRepository.delete(category);
    }

    private CategoryDto mapToDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
            dto.setParentName(category.getParent().getName());
        }
        return dto;
    }

    private CategoryDto mapToDtoWithChildren(Category category) {
        CategoryDto dto = mapToDto(category);
        dto.setChildren(category.getChildren().stream()
                .map(this::mapToDtoWithChildren)
                .collect(Collectors.toList()));
        return dto;
    }
}