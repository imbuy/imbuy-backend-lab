package imbuy.backend.dtoTests;

import imbuy.backend.dto.CreateBidDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateBidDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void createBidDto_ValidData_ShouldPassValidation() {
        CreateBidDto dto = new CreateBidDto(new BigDecimal("100.5"));
        Set<ConstraintViolation<CreateBidDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void createBidDto_NullAmount_ShouldFailValidation() {
        CreateBidDto dto = new CreateBidDto(null);
        Set<ConstraintViolation<CreateBidDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Amount is required");
    }

    @Test
    void createBidDto_ZeroAmount_ShouldFailValidation() {
        CreateBidDto dto = new CreateBidDto(BigDecimal.ZERO);
        Set<ConstraintViolation<CreateBidDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Amount must be greater than 0");
    }

    @Test
    void createBidDto_EqualsAndHashCode_ShouldWorkCorrectly() {
        CreateBidDto dto1 = new CreateBidDto(new BigDecimal("10"));
        CreateBidDto dto2 = new CreateBidDto(new BigDecimal("10"));
        CreateBidDto diff = new CreateBidDto(new BigDecimal("20"));

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1).isNotEqualTo(diff);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1.hashCode()).isNotEqualTo(diff.hashCode());
    }
}
