package imbuy.backend.dtoTests;
import imbuy.backend.dto.CreateLotDto;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateLotDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void createLotDto_ValidData_ShouldPassValidation() {
        CreateLotDto createLotDto = new CreateLotDto();
        createLotDto.setTitle("Test Lot");
        createLotDto.setDescription("Test Description");
        createLotDto.setStartPrice(new BigDecimal("100.00"));
        createLotDto.setBidStep(new BigDecimal("10.00"));
        createLotDto.setCategoryId(1L);
        createLotDto.setStartDate(LocalDateTime.now().plusHours(1));
        createLotDto.setEndDate(LocalDateTime.now().plusDays(1));

        Set<ConstraintViolation<CreateLotDto>> violations = validator.validate(createLotDto);

        assertThat(violations).isEmpty();
    }

    @Test
    void createLotDto_BlankTitle_ShouldFailValidation() {
        CreateLotDto createLotDto = new CreateLotDto();
        createLotDto.setTitle("");
        createLotDto.setStartPrice(new BigDecimal("100.00"));
        createLotDto.setBidStep(new BigDecimal("10.00"));

        Set<ConstraintViolation<CreateLotDto>> violations = validator.validate(createLotDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Title is required");
    }

    @Test
    void createLotDto_NullStartPrice_ShouldFailValidation() {
        CreateLotDto createLotDto = new CreateLotDto();
        createLotDto.setTitle("Test Lot");
        createLotDto.setStartPrice(null);
        createLotDto.setBidStep(new BigDecimal("10.00"));

        Set<ConstraintViolation<CreateLotDto>> violations = validator.validate(createLotDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Start price is required");
    }

    @Test
    void createLotDto_ZeroStartPrice_ShouldFailValidation() {
        CreateLotDto createLotDto = new CreateLotDto();
        createLotDto.setTitle("Test Lot");
        createLotDto.setStartPrice(BigDecimal.ZERO);
        createLotDto.setBidStep(new BigDecimal("10.00"));

        Set<ConstraintViolation<CreateLotDto>> violations = validator.validate(createLotDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Start price must be greater than 0");
    }

    @Test
    void createLotDto_ZeroBidStep_ShouldFailValidation() {
        CreateLotDto createLotDto = new CreateLotDto();
        createLotDto.setTitle("Test Lot");
        createLotDto.setStartPrice(new BigDecimal("100.00"));
        createLotDto.setBidStep(BigDecimal.ZERO);

        Set<ConstraintViolation<CreateLotDto>> violations = validator.validate(createLotDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Bid step must be greater than 0");
    }

    @Test
    void createLotDto_AllFields_ShouldHaveCorrectValues() {
        LocalDateTime startDate = LocalDateTime.now().plusHours(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        CreateLotDto createLotDto = new CreateLotDto();
        createLotDto.setTitle("Test Lot");
        createLotDto.setDescription("Test Description");
        createLotDto.setStartPrice(new BigDecimal("150.75"));
        createLotDto.setBidStep(new BigDecimal("15.50"));
        createLotDto.setCategoryId(5L);
        createLotDto.setStartDate(startDate);
        createLotDto.setEndDate(endDate);

        assertThat(createLotDto.getTitle()).isEqualTo("Test Lot");
        assertThat(createLotDto.getDescription()).isEqualTo("Test Description");
        assertThat(createLotDto.getStartPrice()).isEqualByComparingTo("150.75");
        assertThat(createLotDto.getBidStep()).isEqualByComparingTo("15.50");
        assertThat(createLotDto.getCategoryId()).isEqualTo(5L);
        assertThat(createLotDto.getStartDate()).isEqualTo(startDate);
        assertThat(createLotDto.getEndDate()).isEqualTo(endDate);
    }

    @Test
    void createLotDto_EqualsAndHashCode_ShouldWorkCorrectly() {
        LocalDateTime startDate = LocalDateTime.now().plusHours(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        CreateLotDto dto1 = new CreateLotDto();
        dto1.setTitle("Test Lot");
        dto1.setDescription("Test Description");
        dto1.setStartPrice(new BigDecimal("100.00"));
        dto1.setBidStep(new BigDecimal("10.00"));
        dto1.setCategoryId(1L);
        dto1.setStartDate(startDate);
        dto1.setEndDate(endDate);

        CreateLotDto dto2 = new CreateLotDto();
        dto2.setTitle("Test Lot");
        dto2.setDescription("Test Description");
        dto2.setStartPrice(new BigDecimal("100.00"));
        dto2.setBidStep(new BigDecimal("10.00"));
        dto2.setCategoryId(1L);
        dto2.setStartDate(startDate);
        dto2.setEndDate(endDate);

        CreateLotDto differentDto = new CreateLotDto();
        differentDto.setTitle("Different Lot");

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1).isNotEqualTo(differentDto);
        assertThat(dto1).isNotEqualTo(null);
        assertThat(dto1).isNotEqualTo("not a CreateLotDto");

        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1.hashCode()).isNotEqualTo(differentDto.hashCode());
    }

    @Test
    void createLotDto_ToString_ShouldContainAllFields() {
        LocalDateTime startDate = LocalDateTime.now().plusHours(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        CreateLotDto createLotDto = new CreateLotDto();
        createLotDto.setTitle("Test Lot");
        createLotDto.setDescription("Test Description");
        createLotDto.setStartPrice(new BigDecimal("150.75"));
        createLotDto.setBidStep(new BigDecimal("15.50"));
        createLotDto.setCategoryId(5L);
        createLotDto.setStartDate(startDate);
        createLotDto.setEndDate(endDate);

        String toString = createLotDto.toString();

        assertThat(toString).contains("title=Test Lot");
        assertThat(toString).contains("description=Test Description");
        assertThat(toString).contains("startPrice=150.75");
        assertThat(toString).contains("bidStep=15.50");
        assertThat(toString).contains("categoryId=5");
        assertThat(toString).contains("startDate=" + startDate);
        assertThat(toString).contains("endDate=" + endDate);
    }
}