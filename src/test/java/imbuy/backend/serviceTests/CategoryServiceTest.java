package imbuy.backend.serviceTests;

import imbuy.backend.domain.Category;
import imbuy.backend.dto.CategoryDto;
import imbuy.backend.dto.CategoryTreeDto;
import imbuy.backend.dto.PageResponse;
import imbuy.backend.mapper.CategoryMapper;
import imbuy.backend.repository.CategoryRepository;
import imbuy.backend.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

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

        // Мокаем маппер, чтобы он вернул DTO
        CategoryDto parentDto = new CategoryDto(1L, "Electronics", null, null, List.of());
        when(categoryMapper.toDtoWithChildren(any(Category.class))).thenReturn(parentDto);

        CategoryTreeDto result = categoryService.getCategoryTree();

        assertThat(result).isNotNull();
        assertThat(result.categories()).hasSize(1);
        assertThat(result.categories().get(0).name()).isEqualTo("Electronics");
    }

    @Test
    void getAllCategories_WithPaginated_ShouldReturnPaginatedCategories() {
        List<Category> categories = List.of(parentCategory, childCategory);
        Page<Category> categoryPage = new PageImpl<>(categories, PageRequest.of(0, 20), categories.size());
        when(categoryRepository.findAll(any(PageRequest.class))).thenReturn(categoryPage);

        when(categoryMapper.mapToDto(any(Category.class)))
                .thenAnswer(invocation -> {
                    Category c = invocation.getArgument(0);
                    return new CategoryDto(c.getId(), c.getName(), null, null, List.of());
                });

        PageResponse<CategoryDto> result = categoryService.getAllCategories(PageRequest.of(0, 20));

        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(2);
        verify(categoryRepository).findAll(any(PageRequest.class));
    }

    @Test
    void getAllCategories_WithEmptyPage_ShouldReturnEmptyPage() {
        Page<Category> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(categoryRepository.findAll(any(PageRequest.class))).thenReturn(emptyPage);

        PageResponse<CategoryDto> result = categoryService.getAllCategories(PageRequest.of(0, 20));

        assertThat(result).isNotNull();
        assertThat(result.content()).isEmpty();
        verify(categoryRepository).findAll(any(PageRequest.class));
    }

    @Test
    void getCategoryById_WithExistingCategory_ShouldReturnCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));
        when(categoryMapper.mapToDto(parentCategory))
                .thenReturn(new CategoryDto(1L, "Electronics", null, null, List.of()));

        CategoryDto result = categoryService.getCategoryById(1L);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Electronics");
    }

    @Test
    void createCategory_WithValidData_ShouldCreateCategory() {
        CategoryDto categoryDto = new CategoryDto(null, "Laptops", 1L, null, List.of());

        when(categoryRepository.existsByNameAndParentId("Laptops", 1L)).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(3L);
            return category;
        });
        when(categoryMapper.mapToDto(any(Category.class)))
                .thenReturn(new CategoryDto(3L, "Laptops", 1L, null, List.of()));

        CategoryDto result = categoryService.createCategory(categoryDto);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Laptops");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_WithDuplicateName_ShouldThrowException() {
        CategoryDto categoryDto = new CategoryDto(null, "Electronics", null, null, List.of());
        when(categoryRepository.existsByNameAndParentId("Electronics", null)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(categoryDto))
                .isInstanceOf(RuntimeException.class);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_WithValidData_ShouldUpdateCategory() {
        CategoryDto categoryDto = new CategoryDto(null, "Updated Electronics", null, null, List.of());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(parentCategory);
        when(categoryMapper.mapToDto(any(Category.class)))
                .thenReturn(new CategoryDto(1L, "Updated Electronics", null, null, List.of()));

        CategoryDto result = categoryService.updateCategory(1L, categoryDto);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Updated Electronics");
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

        assertThatThrownBy(() -> categoryService.deleteCategory(1L))
                .isInstanceOf(RuntimeException.class);
        verify(categoryRepository, never()).delete(any());
    }
}
