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
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(1L);
        categoryDto.setName("Electronics");
        categoryDto.setParentId(2L);
        categoryDto.setParentName("Technology");

        CategoryDto child = new CategoryDto();
        child.setId(3L);
        child.setName("Laptops");
        categoryDto.setChildren(List.of(child));

        Set<ConstraintViolation<CategoryDto>> violations = validator.validate(categoryDto);

        assertThat(violations).isEmpty();
    }

    @Test
    void categoryDto_BlankName_ShouldFailValidation() {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName("");

        Set<ConstraintViolation<CategoryDto>> violations = validator.validate(categoryDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Category name is required");
    }

    @Test
    void categoryDto_NullName_ShouldFailValidation() {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName(null);

        Set<ConstraintViolation<CategoryDto>> violations = validator.validate(categoryDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Category name is required");
    }

    @Test
    void categoryDto_WithChildren_ShouldMaintainHierarchy() {
        CategoryDto parent = new CategoryDto();
        parent.setId(1L);
        parent.setName("Parent");

        CategoryDto child1 = new CategoryDto();
        child1.setId(2L);
        child1.setName("Child 1");
        child1.setParentId(1L);

        CategoryDto child2 = new CategoryDto();
        child2.setId(3L);
        child2.setName("Child 2");
        child2.setParentId(1L);

        parent.setChildren(List.of(child1, child2));

        assertThat(parent.getChildren()).hasSize(2);
        assertThat(parent.getChildren().get(0).getName()).isEqualTo("Child 1");
        assertThat(parent.getChildren().get(1).getName()).isEqualTo("Child 2");
        assertThat(parent.getChildren().get(0).getParentId()).isEqualTo(1L);
    }

    @Test
    void categoryDto_EmptyChildrenList_ShouldWork() {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(1L);
        categoryDto.setName("Test Category");
        categoryDto.setChildren(List.of());

        assertThat(categoryDto.getChildren()).isEmpty();
    }
    @Test
    void categoryDto_EqualsAndHashCode_ShouldWorkCorrectly() {
        CategoryDto categoryDto1 = new CategoryDto();
        categoryDto1.setId(1L);
        categoryDto1.setName("Electronics");
        categoryDto1.setParentId(2L);
        categoryDto1.setParentName("Technology");

        CategoryDto categoryDto2 = new CategoryDto();
        categoryDto2.setId(1L);
        categoryDto2.setName("Electronics");
        categoryDto2.setParentId(2L);
        categoryDto2.setParentName("Technology");

        CategoryDto differentCategoryDto = new CategoryDto();
        differentCategoryDto.setId(3L);
        differentCategoryDto.setName("Books");

        assertThat(categoryDto1).isEqualTo(categoryDto2);
        assertThat(categoryDto1).isNotEqualTo(differentCategoryDto);
        assertThat(categoryDto1).isNotEqualTo(null);
        assertThat(categoryDto1).isNotEqualTo("not a CategoryDto");

        assertThat(categoryDto1.hashCode()).isEqualTo(categoryDto2.hashCode());
        assertThat(categoryDto1.hashCode()).isNotEqualTo(differentCategoryDto.hashCode());
    }

    @Test
    void categoryDto_ToString_ShouldContainAllFields() {
        CategoryDto child = new CategoryDto();
        child.setId(3L);
        child.setName("Laptops");

        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(1L);
        categoryDto.setName("Electronics");
        categoryDto.setParentId(2L);
        categoryDto.setParentName("Technology");
        categoryDto.setChildren(List.of(child));

        String toString = categoryDto.toString();

        assertThat(toString).contains("id=1");
        assertThat(toString).contains("name=Electronics");
        assertThat(toString).contains("parentId=2");
        assertThat(toString).contains("parentName=Technology");
        assertThat(toString).contains("children=");
    }
}