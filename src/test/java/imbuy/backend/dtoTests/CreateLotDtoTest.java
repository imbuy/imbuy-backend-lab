package imbuy.backend.dtoTests;

import imbuy.backend.dto.CreateLotDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateLotDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void createLotDto_ValidData_ShouldPassValidation() {
        CreateLotDto dto = new CreateLotDto(
                "Test Lot",
                "Test Description",
                new BigDecimal("100.00"),
                new BigDecimal("10.00"),
                1L,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );

        Set<ConstraintViolation<CreateLotDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void createLotDto_BlankTitle_ShouldFailValidation() {
        CreateLotDto dto = new CreateLotDto(
                "",
                "Description",
                new BigDecimal("100.00"),
                new BigDecimal("10.00"),
                1L,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );

        Set<ConstraintViolation<CreateLotDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Title is required");
    }

    @Test
    void createLotDto_NullStartPrice_ShouldFailValidation() {
        CreateLotDto dto = new CreateLotDto(
                "Test Lot",
                "Description",
                null,
                new BigDecimal("10.00"),
                1L,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );

        Set<ConstraintViolation<CreateLotDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Start price is required");
    }

    @Test
    void createLotDto_ZeroStartPrice_ShouldFailValidation() {
        CreateLotDto dto = new CreateLotDto(
                "Test Lot",
                "Description",
                BigDecimal.ZERO,
                new BigDecimal("10.00"),
                1L,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );

        Set<ConstraintViolation<CreateLotDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Start price must be greater than 0");
    }

    @Test
    void createLotDto_ZeroBidStep_ShouldFailValidation() {
        CreateLotDto dto = new CreateLotDto(
                "Test Lot",
                "Description",
                new BigDecimal("100.00"),
                BigDecimal.ZERO,
                1L,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );

        Set<ConstraintViolation<CreateLotDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Bid step must be greater than 0");
    }

    @Test
    void createLotDto_AllFields_ShouldHaveCorrectValues() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        CreateLotDto dto = new CreateLotDto(
                "Test Lot",
                "Test Description",
                new BigDecimal("150.75"),
                new BigDecimal("15.50"),
                5L,
                start,
                end
        );

        assertThat(dto.title()).isEqualTo("Test Lot");
        assertThat(dto.description()).isEqualTo("Test Description");
        assertThat(dto.start_price()).isEqualByComparingTo("150.75");
        assertThat(dto.bid_step()).isEqualByComparingTo("15.50");
        assertThat(dto.category_id()).isEqualTo(5L);
        assertThat(dto.start_date()).isEqualTo(start);
        assertThat(dto.end_date()).isEqualTo(end);
    }

    @Test
    void createLotDto_EqualsAndHashCode_ShouldWorkCorrectly() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        CreateLotDto dto1 = new CreateLotDto("Test Lot", "Test Description", new BigDecimal("100.00"), new BigDecimal("10.00"), 1L, start, end);
        CreateLotDto dto2 = new CreateLotDto("Test Lot", "Test Description", new BigDecimal("100.00"), new BigDecimal("10.00"), 1L,start, end);
        CreateLotDto different = new CreateLotDto("Different Lot", "Desc", new BigDecimal("100.00"), new BigDecimal("10.00"), 1L, start, end);

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1).isNotEqualTo(different);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1.hashCode()).isNotEqualTo(different.hashCode());
    }

    @Test
    void createLotDto_ToString_ShouldContainAllFields() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        CreateLotDto dto = new CreateLotDto("Test Lot", "Test Description", new BigDecimal("150.75"), new BigDecimal("15.50"), 5L, start, end);

        String str = dto.toString();
        assertThat(str).contains("Test Lot");
        assertThat(str).contains("Test Description");
        assertThat(str).contains("150.75");
        assertThat(str).contains("15.50");
        assertThat(str).contains("5");
        assertThat(str).contains(start.toString());
        assertThat(str).contains(end.toString());
    }
}
