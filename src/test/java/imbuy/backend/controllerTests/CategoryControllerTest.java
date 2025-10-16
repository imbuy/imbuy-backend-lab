package imbuy.backend.controllerTests;

import imbuy.backend.controller.CategoryController;
import imbuy.backend.dto.CategoryDto;
import imbuy.backend.dto.CategoryTreeDto;
import imbuy.backend.dto.PageResponse;
import imbuy.backend.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private CategoryDto categoryDto;
    private CategoryTreeDto categoryTreeDto;
    private PageResponse<CategoryDto> categoryPageResponse;

    @BeforeEach
    void setUp() {
        categoryDto = new CategoryDto();
        categoryDto.setId(1L);
        categoryDto.setName("Electronics");

        categoryTreeDto = new CategoryTreeDto();

        categoryPageResponse = new PageResponse<>();
        categoryPageResponse.setContent(List.of(categoryDto));
        categoryPageResponse.setCurrentPage(0);
        categoryPageResponse.setPageSize(20);
        categoryPageResponse.setHasNext(false);
        categoryPageResponse.setHasPrevious(false);
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
    void getAllCategoriesPaginated_ShouldReturnPaginatedCategories() {
        when(categoryService.getAllCategories(any(PageRequest.class))).thenReturn(categoryPageResponse);

        ResponseEntity<PageResponse<CategoryDto>> response = categoryController.getAllCategoriesPaginated(0, 20);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals("Electronics", response.getBody().getContent().get(0).getName());
        verify(categoryService).getAllCategories(any(PageRequest.class));
    }

    @Test
    void getAllCategoriesPaginated_WithLargeSize_ShouldLimitTo50() {
        when(categoryService.getAllCategories(any(PageRequest.class))).thenReturn(categoryPageResponse);

        ResponseEntity<PageResponse<CategoryDto>> response = categoryController.getAllCategoriesPaginated(0, 100);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(categoryService).getAllCategories(PageRequest.of(0, 50));
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