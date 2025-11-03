package imbuy.backend.dtoTests;

import imbuy.backend.dto.LotDto;
import imbuy.backend.enums.LotStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LotDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void lotDto_ValidData_ShouldPassValidation() {
        LotDto lotDto = new LotDto(
                1L,
                "Test Lot",
                "Test Description",
                new BigDecimal("100.00"),
                new BigDecimal("150.00"),
                new BigDecimal("10.00"),
                1L,
                "owner123",
                2L,
                "Electronics",
                LotStatus.ACTIVE,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now(),
                5,
                true,
                null,
                3L,
                "winner123"
        );

        Set<ConstraintViolation<LotDto>> violations = validator.validate(lotDto);

        assertThat(violations).isEmpty();
    }

    @Test
    void lotDto_BlankTitle_ShouldFailValidation() {
        LotDto lotDto = new LotDto(
                1L,
                "",
                "Description",
                new BigDecimal("100.00"),
                new BigDecimal("150.00"),
                new BigDecimal("10.00"),
                1L,
                "owner",
                2L,
                "Category",
                LotStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now(),
                0,
                false,
                null,
                null,
                null
        );

        Set<ConstraintViolation<LotDto>> violations = validator.validate(lotDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Title is required");
    }

    @Test
    void lotDto_AllFields_ShouldHaveCorrectValues() {
        LocalDateTime startDate = LocalDateTime.now().minusHours(1);
        LocalDateTime endDate = LocalDateTime.now().plusHours(1);
        LocalDateTime createdAt = LocalDateTime.now();

        LotDto lotDto = new LotDto(
                1L,
                "Test Lot",
                "Test Description",
                new BigDecimal("200.00"),
                new BigDecimal("250.00"),
                new BigDecimal("20.00"),
                10L,
                "testowner",
                5L,
                "Books",
                LotStatus.COMPLETED,
                startDate,
                endDate,
                createdAt,
                15,
                false,
                "Invalid description",
                20L,
                "winneruser"
        );

        assertThat(lotDto.id()).isEqualTo(1L);
        assertThat(lotDto.title()).isEqualTo("Test Lot");
        assertThat(lotDto.description()).isEqualTo("Test Description");
        assertThat(lotDto.start_price()).isEqualByComparingTo("200.00");
        assertThat(lotDto.current_price()).isEqualByComparingTo("250.00");
        assertThat(lotDto.bid_step()).isEqualByComparingTo("20.00");
        assertThat(lotDto.owner_id()).isEqualTo(10L);
        assertThat(lotDto.owner_username()).isEqualTo("testowner");
        assertThat(lotDto.category_id()).isEqualTo(5L);
        assertThat(lotDto.category_name()).isEqualTo("Books");
        assertThat(lotDto.status()).isEqualTo(LotStatus.COMPLETED);
        assertThat(lotDto.start_date()).isEqualTo(startDate);
        assertThat(lotDto.end_date()).isEqualTo(endDate);
        assertThat(lotDto.created_at()).isEqualTo(createdAt);
        assertThat(lotDto.bid_count()).isEqualTo(15);
        assertThat(lotDto.is_favorite()).isFalse();
        assertThat(lotDto.rejection_reason()).isEqualTo("Invalid description");
        assertThat(lotDto.winner_id()).isEqualTo(20L);
        assertThat(lotDto.winner_username()).isEqualTo("winneruser");
    }

    @Test
    void lotDto_EqualsAndHashCode_ShouldWorkCorrectly() {
        LocalDateTime startDate = LocalDateTime.now().minusHours(1);
        LocalDateTime endDate = LocalDateTime.now().plusHours(1);
        LocalDateTime createdAt = LocalDateTime.now();

        LotDto dto1 = new LotDto(
                1L, "Test Lot", null, new BigDecimal("100.00"), null,
                new BigDecimal("10.00"), null, null, null, null,
                LotStatus.ACTIVE, startDate, endDate, createdAt, null, null,
                null, null, null
        );

        LotDto dto2 = new LotDto(
                1L, "Test Lot", null, new BigDecimal("100.00"), null,
                new BigDecimal("10.00"), null, null, null, null,
                LotStatus.ACTIVE, startDate, endDate, createdAt, null, null,
                null, null, null
        );

        LotDto differentDto = new LotDto(
                2L, "Different Lot", null, null, null, null, null,
                null, null, null, null, null, null, null,
                null, null, null, null, null
        );

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1).isNotEqualTo(differentDto);
        assertThat(dto1).isNotEqualTo(null);
        assertThat(dto1).isNotEqualTo("not a LotDto");

        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1.hashCode()).isNotEqualTo(differentDto.hashCode());
    }

    @Test
    void lotDto_ToString_ShouldContainAllFields() {
        LocalDateTime startDate = LocalDateTime.now().minusHours(1);
        LocalDateTime endDate = LocalDateTime.now().plusHours(1);
        LocalDateTime createdAt = LocalDateTime.now();

        LotDto lotDto = new LotDto(
                1L,
                "Test Lot",
                "Test Description",
                new BigDecimal("200.00"),
                new BigDecimal("250.00"),
                new BigDecimal("20.00"),
                10L,
                "testowner",
                5L,
                "Books",
                LotStatus.COMPLETED,
                startDate,
                endDate,
                createdAt,
                15,
                false,
                "Invalid description",
                20L,
                "winneruser"
        );

        String toString = lotDto.toString();

        assertThat(toString).contains("id=1");
        assertThat(toString).contains("title=Test Lot");
        assertThat(toString).contains("description=Test Description");
        assertThat(toString).contains("start_price=200.00");
        assertThat(toString).contains("current_price=250.00");
        assertThat(toString).contains("bid_step=20.00");
        assertThat(toString).contains("owner_id=10");
        assertThat(toString).contains("owner_username=testowner");
        assertThat(toString).contains("category_id=5");
        assertThat(toString).contains("category_name=Books");
        assertThat(toString).contains("status=COMPLETED");
        assertThat(toString).contains("start_date=" + startDate);
        assertThat(toString).contains("end_date=" + endDate);
        assertThat(toString).contains("created_at=" + createdAt);
        assertThat(toString).contains("bid_count=15");
        assertThat(toString).contains("is_favorite=false");
        assertThat(toString).contains("rejection_reason=Invalid description");
        assertThat(toString).contains("winner_id=20");
        assertThat(toString).contains("winner_username=winneruser");
    }
}
