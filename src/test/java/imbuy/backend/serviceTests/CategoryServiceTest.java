package imbuy.backend.serviceTests;

import imbuy.backend.domain.Category;
import imbuy.backend.dto.CategoryDto;
import imbuy.backend.dto.CategoryTreeDto;
import imbuy.backend.repository.CategoryRepository;
import imbuy.backend.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category parentCategory;
    private Category childCategory;

    @BeforeEach
    void setUp() {
        parentCategory = new Category("Electronics");
        parentCategory.setId(1L);

        childCategory = new Category("Smartphones");
        childCategory.setId(2L);
        childCategory.setParent(parentCategory);
        parentCategory.getChildren().add(childCategory);
    }

    @Test
    void getCategoryTree_ShouldReturnTreeStructure() {
        when(categoryRepository.findRootCategoriesWithChildren()).thenReturn(List.of(parentCategory));

        CategoryTreeDto result = categoryService.getCategoryTree();

        assertNotNull(result);
        assertEquals(1, result.getCategories().size());
        assertEquals("Electronics", result.getCategories().get(0).getName());
        assertEquals(1, result.getCategories().get(0).getChildren().size());
        assertEquals("Smartphones", result.getCategories().get(0).getChildren().get(0).getName());
    }

    @Test
    void getAllCategories_ShouldReturnAllCategories() {
        when(categoryRepository.findAll()).thenReturn(List.of(parentCategory, childCategory));

        List<CategoryDto> result = categoryService.getAllCategories();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getCategoryById_WithExistingCategory_ShouldReturnCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));

        CategoryDto result = categoryService.getCategoryById(1L);

        assertNotNull(result);
        assertEquals("Electronics", result.getName());
    }

    @Test
    void createCategory_WithValidData_ShouldCreateCategory() {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName("Laptops");
        categoryDto.setParentId(1L);

        when(categoryRepository.existsByNameAndParentId("Laptops", 1L)).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(3L);
            return category;
        });

        CategoryDto result = categoryService.createCategory(categoryDto);

        assertNotNull(result);
        assertEquals("Laptops", result.getName());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_WithDuplicateName_ShouldThrowException() {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName("Electronics");
        categoryDto.setParentId(null);

        when(categoryRepository.existsByNameAndParentId("Electronics", null)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> categoryService.createCategory(categoryDto));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_WithValidData_ShouldUpdateCategory() {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName("Updated Electronics");
        categoryDto.setParentId(null);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(parentCategory);

        CategoryDto result = categoryService.updateCategory(1L, categoryDto);

        assertNotNull(result);
        assertEquals("Updated Electronics", result.getName());
        verify(categoryRepository).save(parentCategory);
    }

    @Test
    void deleteCategory_WithoutChildrenOrLots_ShouldDeleteCategory() {
        Category category = new Category("ToDelete");
        category.setId(3L);

        when(categoryRepository.findById(3L)).thenReturn(Optional.of(category));

        categoryService.deleteCategory(3L);

        verify(categoryRepository).delete(category);
    }

    @Test
    void deleteCategory_WithChildren_ShouldThrowException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));

        assertThrows(RuntimeException.class, () -> categoryService.deleteCategory(1L));
        verify(categoryRepository, never()).delete(any());
    }
}