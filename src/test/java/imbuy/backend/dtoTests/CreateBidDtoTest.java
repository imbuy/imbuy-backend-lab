package imbuy.backend.dtoTests;

import imbuy.backend.dto.CreateBidDto;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateBidDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void createBidDto_ValidData_ShouldPassValidation() {
        CreateBidDto createBidDto = new CreateBidDto();
        createBidDto.setAmount(new BigDecimal("100.50"));

        Set<ConstraintViolation<CreateBidDto>> violations = validator.validate(createBidDto);

        assertThat(violations).isEmpty();
    }

    @Test
    void createBidDto_NullAmount_ShouldFailValidation() {
        CreateBidDto createBidDto = new CreateBidDto();
        createBidDto.setAmount(null);

        Set<ConstraintViolation<CreateBidDto>> violations = validator.validate(createBidDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Amount is required");
    }

    @Test
    void createBidDto_ZeroAmount_ShouldFailValidation() {
        CreateBidDto createBidDto = new CreateBidDto();
        createBidDto.setAmount(BigDecimal.ZERO);

        Set<ConstraintViolation<CreateBidDto>> violations = validator.validate(createBidDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Amount must be greater than 0");
    }

    @Test
    void createBidDto_NegativeAmount_ShouldFailValidation() {
        CreateBidDto createBidDto = new CreateBidDto();
        createBidDto.setAmount(new BigDecimal("-5.00"));

        Set<ConstraintViolation<CreateBidDto>> violations = validator.validate(createBidDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Amount must be greater than 0");
    }

    @Test
    void createBidDto_EqualsAndHashCode_ShouldWorkCorrectly() {
        CreateBidDto dto1 = new CreateBidDto();
        dto1.setAmount(new BigDecimal("100.50"));

        CreateBidDto dto2 = new CreateBidDto();
        dto2.setAmount(new BigDecimal("100.50"));

        CreateBidDto differentDto = new CreateBidDto();
        differentDto.setAmount(new BigDecimal("200.00"));

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1).isNotEqualTo(differentDto);
        assertThat(dto1).isNotEqualTo(null);
        assertThat(dto1).isNotEqualTo("not a CreateBidDto");

        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1.hashCode()).isNotEqualTo(differentDto.hashCode());
    }

    @Test
    void createBidDto_ToString_ShouldContainAmount() {
        CreateBidDto createBidDto = new CreateBidDto();
        createBidDto.setAmount(new BigDecimal("150.75"));

        String toString = createBidDto.toString();

        assertThat(toString).contains("amount=150.75");
    }
}
