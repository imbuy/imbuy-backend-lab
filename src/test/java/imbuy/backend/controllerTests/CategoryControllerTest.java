package imbuy.backend.controllerTests;

import imbuy.backend.controller.CategoryController;
import imbuy.backend.dto.CategoryDto;
import imbuy.backend.dto.CategoryTreeDto;
import imbuy.backend.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private CategoryDto categoryDto;
    private CategoryTreeDto categoryTreeDto;

    @BeforeEach
    void setUp() {
        categoryDto = new CategoryDto();
        categoryDto.setId(1L);
        categoryDto.setName("Electronics");

        categoryTreeDto = new CategoryTreeDto();
    }

    @Test
    void getCategoryTree_ShouldReturnTree() {
        when(categoryService.getCategoryTree()).thenReturn(categoryTreeDto);

        ResponseEntity<CategoryTreeDto> response = categoryController.getCategoryTree();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(categoryTreeDto, response.getBody());
        verify(categoryService).getCategoryTree();
    }

    @Test
    void getAllCategories_ShouldReturnAllCategories() {
        List<CategoryDto> categories = List.of(categoryDto);
        when(categoryService.getAllCategories()).thenReturn(categories);

        ResponseEntity<List<CategoryDto>> response = categoryController.getAllCategories();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Electronics", response.getBody().get(0).getName());
        verify(categoryService).getAllCategories();
    }

    @Test
    void getCategoryById_WithExistingCategory_ShouldReturnCategory() {
        when(categoryService.getCategoryById(1L)).thenReturn(categoryDto);

        ResponseEntity<CategoryDto> response = categoryController.getCategoryById(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(categoryDto, response.getBody());
        verify(categoryService).getCategoryById(1L);
    }

    @Test
    void createCategory_WithValidData_ShouldCreateCategory() {
        when(categoryService.createCategory(categoryDto)).thenReturn(categoryDto);

        ResponseEntity<CategoryDto> response = categoryController.createCategory(categoryDto);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(categoryDto, response.getBody());
        verify(categoryService).createCategory(categoryDto);
    }

    @Test
    void updateCategory_WithValidData_ShouldUpdateCategory() {
        when(categoryService.updateCategory(1L, categoryDto)).thenReturn(categoryDto);

        ResponseEntity<CategoryDto> response = categoryController.updateCategory(1L, categoryDto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(categoryDto, response.getBody());
        verify(categoryService).updateCategory(1L, categoryDto);
    }

    @Test
    void deleteCategory_WithExistingCategory_ShouldDeleteCategory() {
        doNothing().when(categoryService).deleteCategory(1L);

        ResponseEntity<Void> response = categoryController.deleteCategory(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(categoryService).deleteCategory(1L);
    }
}