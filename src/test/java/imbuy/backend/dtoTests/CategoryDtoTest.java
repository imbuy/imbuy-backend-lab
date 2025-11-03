package imbuy.backend.dtoTests;

import imbuy.backend.dto.CategoryDto;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void categoryDto_ValidData_ShouldPassValidation() {
        CategoryDto child = new CategoryDto(3L, "Laptops", null, null, List.of());
        CategoryDto categoryDto = new CategoryDto(1L, "Electronics", 2L, "Technology", List.of(child));

        Set<ConstraintViolation<CategoryDto>> violations = validator.validate(categoryDto);
        assertThat(violations).isEmpty();
    }

    @Test
    void categoryDto_BlankName_ShouldFailValidation() {
        CategoryDto categoryDto = new CategoryDto(1L, "", null, null, List.of());

        Set<ConstraintViolation<CategoryDto>> violations = validator.validate(categoryDto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Category name is required");
    }

    @Test
    void categoryDto_NullName_ShouldFailValidation() {
        CategoryDto categoryDto = new CategoryDto(1L, null, null, null, List.of());

        Set<ConstraintViolation<CategoryDto>> violations = validator.validate(categoryDto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Category name is required");
    }

    @Test
    void categoryDto_WithChildren_ShouldMaintainHierarchy() {
        CategoryDto child1 = new CategoryDto(2L, "Child 1", 1L, null, List.of());
        CategoryDto child2 = new CategoryDto(3L, "Child 2", 1L, null, List.of());
        CategoryDto parent = new CategoryDto(1L, "Parent", null, null, List.of(child1, child2));

        assertThat(parent.children()).hasSize(2);
        assertThat(parent.children().get(0).name()).isEqualTo("Child 1");
        assertThat(parent.children().get(1).name()).isEqualTo("Child 2");
        assertThat(parent.children().get(0).parent_id()).isEqualTo(1L);
    }

    @Test
    void categoryDto_EmptyChildrenList_ShouldWork() {
        CategoryDto categoryDto = new CategoryDto(1L, "Test Category", null, null, List.of());
        assertThat(categoryDto.children()).isEmpty();
    }

    @Test
    void categoryDto_EqualsAndHashCode_ShouldWorkCorrectly() {
        CategoryDto categoryDto1 = new CategoryDto(1L, "Electronics", 2L, "Technology", List.of());
        CategoryDto categoryDto2 = new CategoryDto(1L, "Electronics", 2L, "Technology", List.of());
        CategoryDto differentCategoryDto = new CategoryDto(3L, "Books", null, null, List.of());

        assertThat(categoryDto1).isEqualTo(categoryDto2);
        assertThat(categoryDto1).isNotEqualTo(differentCategoryDto);
        assertThat(categoryDto1).isNotEqualTo(null);
        assertThat(categoryDto1).isNotEqualTo("not a CategoryDto");

        assertThat(categoryDto1.hashCode()).isEqualTo(categoryDto2.hashCode());
        assertThat(categoryDto1.hashCode()).isNotEqualTo(differentCategoryDto.hashCode());
    }

    @Test
    void categoryDto_ToString_ShouldContainAllFields() {
        CategoryDto child = new CategoryDto(3L, "Laptops", null, null, List.of());
        CategoryDto categoryDto = new CategoryDto(1L, "Electronics", 2L, "Technology", List.of(child));

        String toString = categoryDto.toString();
        assertThat(toString).contains("id=1");
        assertThat(toString).contains("name=Electronics");
        assertThat(toString).contains("parent_id=2");
        assertThat(toString).contains("parent_name=Technology");
        assertThat(toString).contains("children=");
    }
}
